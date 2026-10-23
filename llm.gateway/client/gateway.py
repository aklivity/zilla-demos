import json
import os
import time

import anthropic
import boto3
import botocore.config
import botocore.exceptions
import httpx
import openai
from google import genai
from google.genai import errors as genai_errors
from google.genai import types as genai_types
from google.oauth2.credentials import Credentials as GoogleCredentials

OPENAI_BASE_URL = os.environ.get("ZILLA_OPENAI_BASE_URL", "http://zilla:7161/v1")
ANTHROPIC_BASE_URL = os.environ.get("ZILLA_ANTHROPIC_BASE_URL", "http://zilla:7162")
GEMINI_BASE_URL = os.environ.get("ZILLA_GEMINI_BASE_URL", "http://zilla:7163")
BEDROCK_BASE_URL = os.environ.get("ZILLA_BEDROCK_BASE_URL", "http://zilla:7164")
AZURE_BASE_URL = os.environ.get("ZILLA_AZURE_BASE_URL", "http://zilla:7165")
VERTEX_BASE_URL = os.environ.get("ZILLA_VERTEX_BASE_URL", "http://zilla:7166")

SDK_OPENAI = "OpenAI SDK"
SDK_ANTHROPIC = "Anthropic SDK"
SDK_GEMINI = "Gemini SDK"
SDK_BEDROCK = "AWS SDK (Bedrock)"
SDK_AZURE = "Azure OpenAI SDK"
SDK_VERTEX = "Vertex AI SDK"
SDKS = [SDK_OPENAI, SDK_ANTHROPIC, SDK_GEMINI, SDK_BEDROCK, SDK_AZURE, SDK_VERTEX]

SDK_INFO = {
    SDK_OPENAI: ("openai", "OpenAI API", "7161", OPENAI_BASE_URL),
    SDK_ANTHROPIC: ("anthropic", "Anthropic API", "7162", ANTHROPIC_BASE_URL),
    SDK_GEMINI: ("gemini", "Gemini API", "7163", GEMINI_BASE_URL),
    SDK_BEDROCK: ("aws-bedrock", "Bedrock API", "7164", BEDROCK_BASE_URL),
    SDK_AZURE: ("openai", "Azure OpenAI API", "7165", AZURE_BASE_URL),
    SDK_VERTEX: ("gemini", "Vertex AI API", "7166", VERTEX_BASE_URL),
}

PROVIDERS = [
    ("gpt-", "OpenAI", "openai", "OPENAI_API_KEY"),
    ("claude-", "Anthropic", "anthropic", "ANTHROPIC_API_KEY"),
    ("gemini-", "Gemini", "gemini", "GEMINI_API_KEY"),
    ("anthropic.", "AWS Bedrock", "aws-bedrock", None),
    ("amazon.", "AWS Bedrock", "aws-bedrock", None),
    ("us.anthropic.", "AWS Bedrock", "aws-bedrock", None),
    ("us.amazon.", "AWS Bedrock", "aws-bedrock", None),
    ("azure-", "Azure AI Foundry", "azure-foundry", "AZURE_OPENAI_API_KEY"),
    ("meta/", "Google Vertex AI", "gcp-vertex", None),
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

GEMINI_TOOLS = [genai_types.Tool(function_declarations=[genai_types.FunctionDeclaration(
    name="get_weather",
    description="Get the current weather for a city",
    parameters_json_schema=ANTHROPIC_TOOLS[0]["input_schema"])])]

BEDROCK_TOOLS = {"tools": [{"toolSpec": {
    "name": "get_weather",
    "description": "Get the current weather for a city",
    "inputSchema": {"json": ANTHROPIC_TOOLS[0]["input_schema"]}}}]}


def provider_for(model):
    for prefix, name, dialect, key_env in PROVIDERS:
        if model.startswith(prefix):
            return name, dialect, key_env
    return None, None, None


def api_key_for(sdk, model, overrides):
    name, dialect, key_env = provider_for(model)
    if sdk == SDK_BEDROCK or (name is not None and key_env is None):
        return "unused"
    if key_env is None:
        return None
    return (overrides or {}).get(key_env) or os.environ.get(key_env)


def route_summary(sdk, model):
    name, dialect, _ = provider_for(model)
    if name is None:
        return f"`{model}` matches no route in `zilla.yaml`, so Zilla rejects it."
    sdk_dialect, api, port, _ = SDK_INFO[sdk]
    translated = "translated" if dialect != sdk_dialect else "no translation"
    return f"{api} → Zilla :{port} → {name} ({translated})"


def chat(sdk, model, prompt, stream, tools, max_tokens, overrides=None):
    model = (model or "").strip()
    route = route_summary(sdk, model)
    key = api_key_for(sdk, model, overrides)
    if provider_for(model)[0] is not None and not key:
        _, _, key_env = provider_for(model)
        yield "", f"**Route:** {route}  \n**Error:** set `{key_env}` or enter it under API keys."
        return

    if tools:
        stream = False

    started = time.monotonic()
    text, usage = "", None
    try:
        calls = {SDK_OPENAI: _openai_chat, SDK_ANTHROPIC: _anthropic_chat,
                 SDK_GEMINI: _gemini_chat, SDK_BEDROCK: _bedrock_chat,
                 SDK_AZURE: _azure_chat, SDK_VERTEX: _vertex_chat}[sdk]
        for text, usage in calls(model, prompt, stream, tools, max_tokens, key or "none"):
            yield text, _meta(route, usage, started, done=False)
        yield text, _meta(route, usage, started, done=True)
    except (openai.APIStatusError, anthropic.APIStatusError) as ex:
        yield text, f"**Route:** {route}  \n**Error {ex.status_code}:** {_error_message(ex)}"
    except genai_errors.APIError as ex:
        yield text, f"**Route:** {route}  \n**Error {ex.code}:** {ex.message or ex}"
    except botocore.exceptions.ClientError as ex:
        status = ex.response.get("ResponseMetadata", {}).get("HTTPStatusCode", "")
        yield text, f"**Route:** {route}  \n**Error {status}:** {ex.response.get('Error', {}).get('Message') or ex}"
    except (openai.APIConnectionError, anthropic.APIConnectionError, httpx.TransportError,
            botocore.exceptions.EndpointConnectionError, botocore.exceptions.ConnectionClosedError):
        base = SDK_INFO[sdk][3]
        hint = ""
        if sdk in (SDK_BEDROCK, SDK_AZURE, SDK_VERTEX) or provider_for(model)[1] in ("aws-bedrock", "azure-foundry", "gcp-vertex"):
            hint = " The AWS, Azure OpenAI and Vertex AI SDKs, and AWS Bedrock, Azure AI Foundry and Google Vertex AI models, need Part 2 (Zilla Plus) first."
        yield text, (f"**Route:** {route}  \n**Error:** no response from Zilla at `{base}`. "
                     f"Check that Zilla is running and that `{model}` matches a route in `zilla.yaml`.{hint}")


def _azure_chat(model, prompt, stream, tools, max_tokens, key):
    client = openai.AzureOpenAI(azure_endpoint=AZURE_BASE_URL, api_key=key, api_version="2024-10-21",
                                max_retries=0, timeout=120)
    yield from _openai_chat(model, prompt, stream, tools, max_tokens, key, client)


def _openai_chat(model, prompt, stream, tools, max_tokens, key, client=None):
    client = client or openai.OpenAI(base_url=OPENAI_BASE_URL, api_key=key, max_retries=0, timeout=120)
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


def _vertex_chat(model, prompt, stream, tools, max_tokens, key):
    client = genai.Client(
        vertexai=True, project="zilla-demo", location="us-central1", credentials=GoogleCredentials(token=key),
        http_options=genai_types.HttpOptions(base_url=VERTEX_BASE_URL, api_version="v1", timeout=120_000,
                                             retry_options=genai_types.HttpRetryOptions(attempts=1)))
    yield from _gemini_chat(model, prompt, stream, tools, max_tokens, key, client)


def _gemini_chat(model, prompt, stream, tools, max_tokens, key, client=None):
    client = client or genai.Client(api_key=key, http_options=genai_types.HttpOptions(
        base_url=GEMINI_BASE_URL, api_version="v1beta", timeout=120_000,
        retry_options=genai_types.HttpRetryOptions(attempts=1)))
    config = genai_types.GenerateContentConfig(max_output_tokens=max_tokens)
    if tools:
        config.tools = GEMINI_TOOLS
        config.automatic_function_calling = genai_types.AutomaticFunctionCallingConfig(disable=True)

    if not stream:
        resp = client.models.generate_content(model=model, contents=prompt, config=config)
        calls = [(c.name, json.dumps(c.args)) for c in resp.function_calls or []]
        yield _with_tool_calls(_gemini_text(resp), calls), _gemini_usage(resp)
        return

    text, usage = "", None
    for chunk in client.models.generate_content_stream(model=model, contents=prompt, config=config):
        usage = _gemini_usage(chunk) or usage
        text += _gemini_text(chunk)
        yield text, usage
    yield text, usage


def _gemini_text(resp):
    return "".join(part.text for candidate in resp.candidates or []
                   for part in ((candidate.content.parts if candidate.content else None) or [])
                   if part.text and not part.thought)


def _gemini_usage(resp):
    meta = resp.usage_metadata
    if not meta or not meta.prompt_token_count:
        return None
    return meta.prompt_token_count, (meta.candidates_token_count or 0) + (meta.thoughts_token_count or 0)


def _bedrock_chat(model, prompt, stream, tools, max_tokens, key):
    client = boto3.client(
        "bedrock-runtime", endpoint_url=BEDROCK_BASE_URL, region_name="us-east-1",
        aws_access_key_id="zilla", aws_secret_access_key="zilla",
        config=botocore.config.Config(retries={"max_attempts": 1}, read_timeout=120, connect_timeout=5))
    kwargs = {
        "modelId": model,
        "messages": [{"role": "user", "content": [{"text": prompt}]}],
        "inferenceConfig": {"maxTokens": max_tokens},
    }
    if tools:
        kwargs["toolConfig"] = BEDROCK_TOOLS

    if not stream:
        resp = client.converse(**kwargs)
        content = resp.get("output", {}).get("message", {}).get("content", [])
        text = "".join(block.get("text", "") for block in content)
        calls = [(b["toolUse"]["name"], json.dumps(b["toolUse"].get("input"))) for b in content if "toolUse" in b]
        usage = resp.get("usage")
        yield _with_tool_calls(text, calls), ((usage.get("inputTokens"), usage.get("outputTokens")) if usage else None)
        return

    text, usage = "", None
    for event in client.converse_stream(**kwargs)["stream"]:
        delta = event.get("contentBlockDelta", {}).get("delta", {})
        if delta.get("text"):
            text += delta["text"]
        metadata = event.get("metadata", {}).get("usage")
        if metadata:
            usage = (metadata.get("inputTokens"), metadata.get("outputTokens"))
        yield text, usage
    yield text, usage


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
