import json
import os
import time

import anthropic
import openai

OPENAI_BASE_URL = os.environ.get("ZILLA_OPENAI_BASE_URL", "http://zilla:7161/v1")
ANTHROPIC_BASE_URL = os.environ.get("ZILLA_ANTHROPIC_BASE_URL", "http://zilla:7162")

SDK_OPENAI = "OpenAI SDK"
SDK_ANTHROPIC = "Anthropic SDK"
SDKS = [SDK_OPENAI, SDK_ANTHROPIC]

PROVIDERS = [
    ("gpt-", "OpenAI", "openai", "OPENAI_API_KEY"),
    ("claude-", "Anthropic", "anthropic", "ANTHROPIC_API_KEY"),
    ("anthropic.", "AWS Bedrock", "aws-bedrock", None),
    ("amazon.", "AWS Bedrock", "aws-bedrock", None),
    ("us.anthropic.", "AWS Bedrock", "aws-bedrock", None),
    ("us.amazon.", "AWS Bedrock", "aws-bedrock", None),
]

OPENAI_TOOLS = [{
    "type": "function",
    "function": {
        "name": "get_weather",
        "description": "Get the current weather for a city",
        "parameters": {
            "type": "object",
            "properties": {"city": {"type": "string"}},
            "required": ["city"],
        },
    },
}]

ANTHROPIC_TOOLS = [{
    "name": "get_weather",
    "description": "Get the current weather for a city",
    "input_schema": {
        "type": "object",
        "properties": {"city": {"type": "string"}},
        "required": ["city"],
    },
}]


def provider_for(model):
    for prefix, name, dialect, key_env in PROVIDERS:
        if model.startswith(prefix):
            return name, dialect, key_env
    return None, None, None


def api_key_for(model, overrides):
    _, dialect, key_env = provider_for(model)
    if dialect == "aws-bedrock":
        return "unused"
    if key_env is None:
        return None
    return (overrides or {}).get(key_env) or os.environ.get(key_env)


def route_summary(sdk, model):
    name, dialect, _ = provider_for(model)
    if name is None:
        return f"`{model}` matches no route in `zilla.yaml`, so Zilla rejects it."
    sdk_dialect = "openai" if sdk == SDK_OPENAI else "anthropic"
    port = "7161" if sdk == SDK_OPENAI else "7162"
    api = "OpenAI API" if sdk == SDK_OPENAI else "Anthropic API"
    translated = "translated" if dialect != sdk_dialect else "no translation"
    return f"{api} → Zilla :{port} → {name} ({translated})"


def chat(sdk, model, prompt, stream, tools, max_tokens, overrides=None):
    model = (model or "").strip()
    route = route_summary(sdk, model)
    key = api_key_for(model, overrides)
    if provider_for(model)[0] is not None and not key:
        _, _, key_env = provider_for(model)
        yield "", f"**Route:** {route}  \n**Error:** set `{key_env}` or enter it under API keys."
        return

    if tools:
        stream = False

    started = time.monotonic()
    text, usage = "", None
    try:
        calls = _openai_chat if sdk == SDK_OPENAI else _anthropic_chat
        for text, usage in calls(model, prompt, stream, tools, max_tokens, key or "none"):
            yield text, _meta(route, usage, started, done=False)
        yield text, _meta(route, usage, started, done=True)
    except (openai.APIStatusError, anthropic.APIStatusError) as ex:
        yield text, f"**Route:** {route}  \n**Error {ex.status_code}:** {_error_message(ex)}"
    except (openai.APIConnectionError, anthropic.APIConnectionError):
        base = OPENAI_BASE_URL if sdk == SDK_OPENAI else ANTHROPIC_BASE_URL
        hint = " AWS Bedrock models need Part 2 (Zilla Plus) first." if provider_for(model)[1] == "aws-bedrock" else ""
        yield text, (f"**Route:** {route}  \n**Error:** no response from Zilla at `{base}`. "
                     f"Check that Zilla is running and that `{model}` matches a route in `zilla.yaml`.{hint}")


def _openai_chat(model, prompt, stream, tools, max_tokens, key):
    client = openai.OpenAI(base_url=OPENAI_BASE_URL, api_key=key, max_retries=0, timeout=120)
    kwargs = {
        "model": model,
        "max_tokens": max_tokens,
        "messages": [{"role": "user", "content": prompt}],
    }
    if tools:
        kwargs["tools"] = OPENAI_TOOLS

    if not stream:
        resp = client.chat.completions.create(**kwargs)
        message = resp.choices[0].message
        calls = [(c.function.name, c.function.arguments) for c in message.tool_calls or []]
        usage = (resp.usage.prompt_tokens, resp.usage.completion_tokens) if resp.usage else None
        yield _with_tool_calls(message.content or "", calls), usage
        return

    kwargs["stream_options"] = {"include_usage": True}

    text, usage, calls = "", None, {}
    for chunk in client.chat.completions.create(**kwargs, stream=True):
        if chunk.usage:
            usage = (chunk.usage.prompt_tokens, chunk.usage.completion_tokens)
        if not chunk.choices:
            continue
        delta = chunk.choices[0].delta
        if delta.content:
            text += delta.content
        for call in delta.tool_calls or []:
            name, arguments = calls.get(call.index, ("", ""))
            if call.function and call.function.name:
                name = call.function.name
            if call.function and call.function.arguments:
                arguments += call.function.arguments
            calls[call.index] = (name, arguments)
        yield _with_tool_calls(text, list(calls.values())), usage
    yield _with_tool_calls(text, list(calls.values())), usage


def _anthropic_chat(model, prompt, stream, tools, max_tokens, key):
    client = anthropic.Anthropic(base_url=ANTHROPIC_BASE_URL, api_key=key, max_retries=0, timeout=120)
    kwargs = {
        "model": model,
        "max_tokens": max_tokens,
        "messages": [{"role": "user", "content": prompt}],
    }
    if tools:
        kwargs["tools"] = ANTHROPIC_TOOLS

    if not stream:
        resp = client.messages.create(**kwargs)
        yield _anthropic_text(resp), (resp.usage.input_tokens, resp.usage.output_tokens)
        return

    text = ""
    with client.messages.stream(**kwargs) as events:
        for delta in events.text_stream:
            text += delta
            yield text, None
        final = events.get_final_message()
    yield _anthropic_text(final), (final.usage.input_tokens, final.usage.output_tokens)


def _anthropic_text(message):
    text = "".join(block.text for block in message.content if block.type == "text")
    calls = [(block.name, json.dumps(block.input)) for block in message.content if block.type == "tool_use"]
    return _with_tool_calls(text, calls)


def _with_tool_calls(text, calls):
    lines = [f"Tool call: `{name}({arguments})`" for name, arguments in calls if name]
    return "\n\n".join([part for part in [text, *lines] if part])


def _meta(route, usage, started, done):
    elapsed = time.monotonic() - started
    if usage and any(usage):
        tokens = f"{usage[0] or '–'} in · {usage[1] or '–'} out"
    else:
        tokens = "…" if not done else "not reported by the provider"
    status = f"{elapsed:.2f} s" if done else "streaming…"
    return f"**Route:** {route}  \n**Tokens:** {tokens}  \n**Time:** {status}"


def _error_message(ex):
    body = ex.body if isinstance(ex.body, dict) else {}
    error = body.get("error", body)
    if isinstance(error, dict) and error.get("message"):
        return error["message"]
    return str(ex)
