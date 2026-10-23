import os

import gradio as gr

from gateway import (ANTHROPIC_BASE_URL, AZURE_BASE_URL, BEDROCK_BASE_URL, GEMINI_BASE_URL, OPENAI_BASE_URL,
                     SDK_OPENAI, SDKS, VERTEX_BASE_URL, chat)

MODELS = [m.strip() for m in os.environ.get(
    "MODELS", "gpt-4o-mini,claude-haiku-4-5,gemini-3.5-flash-lite,claude-primary,us.anthropic.claude-haiku-4-5-20251001-v1:0,amazon.nova-micro-v1:0,azure-gpt-4o-mini,meta/llama-3.3-70b-instruct-maas").split(",") if m.strip()]

INTRO = f"""
# Zilla LLM Gateway


Send the same prompt through Zilla with the OpenAI, Anthropic or Gemini SDK, or with the AWS, Azure OpenAI or Vertex AI SDK in Part 2.
Zilla routes each request by model (`gpt-*` to OpenAI, `claude-*` to Anthropic, `gemini-*` to Gemini,
`anthropic.*`, `amazon.*`, `us.anthropic.*` and `us.amazon.*` to AWS Bedrock, `azure-*` to Azure AI Foundry and `meta/*` to Google Vertex AI with Zilla Plus) and translates between APIs when needed.

This app calls Zilla at `{OPENAI_BASE_URL}` (OpenAI SDK), `{ANTHROPIC_BASE_URL}` (Anthropic SDK),
`{GEMINI_BASE_URL}` (Gemini SDK), `{BEDROCK_BASE_URL}` (AWS SDK), `{AZURE_BASE_URL}` (Azure OpenAI SDK)
and `{VERTEX_BASE_URL}` (Vertex AI SDK).
"""


def key_placeholder(env):
    return f"Using ${env}" if os.environ.get(env) else f"${env} is not set"


def keys(openai_key, anthropic_key, gemini_key, azure_key):
    return {"OPENAI_API_KEY": openai_key, "ANTHROPIC_API_KEY": anthropic_key, "GEMINI_API_KEY": gemini_key,
            "AZURE_OPENAI_API_KEY": azure_key}


def send(sdk, model, prompt, stream, tools, max_tokens, openai_key, anthropic_key, gemini_key, azure_key):
    yield from chat(sdk, model, prompt, stream, tools, int(max_tokens), keys(openai_key, anthropic_key, gemini_key, azure_key))


def side(sdk, model, prompt, stream, max_tokens, openai_key, anthropic_key, gemini_key, azure_key):
    yield from chat(sdk, model, prompt, stream, False, int(max_tokens), keys(openai_key, anthropic_key, gemini_key, azure_key))


with gr.Blocks(title="Zilla LLM Gateway") as demo:
    gr.Markdown(INTRO)

    with gr.Accordion("API keys", open=False):
        gr.Markdown("Keys are sent with each request and forwarded by Zilla to the provider. Leave blank to use the exported environment variables.")
        with gr.Row():
            openai_key = gr.Textbox(label="OpenAI API key", type="password", placeholder=key_placeholder("OPENAI_API_KEY"))
            anthropic_key = gr.Textbox(label="Anthropic API key", type="password", placeholder=key_placeholder("ANTHROPIC_API_KEY"))
            gemini_key = gr.Textbox(label="Gemini API key", type="password", placeholder=key_placeholder("GEMINI_API_KEY"))
            azure_key = gr.Textbox(label="Azure OpenAI API key", type="password", placeholder=key_placeholder("AZURE_OPENAI_API_KEY"))

    with gr.Tab("Chat"):
        with gr.Row(equal_height=False):
            with gr.Column(scale=1):
                sdk = gr.Radio(SDKS, value=SDK_OPENAI, label="SDK")
                model = gr.Dropdown(MODELS, value=MODELS[0], label="Model", allow_custom_value=True)
                stream = gr.Checkbox(value=True, label="Stream")
                tools = gr.Checkbox(value=False, label="Send a get_weather tool (replies without streaming)")
                max_tokens = gr.Slider(64, 4096, value=512, step=64, label="Max tokens")
            with gr.Column(scale=2):
                prompt = gr.Textbox(label="Prompt", lines=3, max_lines=8, value="In one sentence, what is an LLM gateway?")
                send_button = gr.Button("Send", variant="primary")
                reply = gr.Markdown(label="Reply", container=True, min_height=160)
                meta = gr.Markdown()
        send_button.click(
            send,
            [sdk, model, prompt, stream, tools, max_tokens, openai_key, anthropic_key, gemini_key, azure_key],
            [reply, meta])

    with gr.Tab("Compare"):
        compare_prompt = gr.Textbox(label="Prompt", lines=3, max_lines=8, value="In one sentence, what is an LLM gateway?")
        with gr.Row():
            compare_stream = gr.Checkbox(value=True, label="Stream")
            compare_max_tokens = gr.Slider(64, 4096, value=512, step=64, label="Max tokens")
        compare_button = gr.Button("Send to both", variant="primary")
        with gr.Row():
            with gr.Column():
                left_sdk = gr.Radio(SDKS, value=SDK_OPENAI, label="SDK")
                left_model = gr.Dropdown(MODELS, value=MODELS[0], label="Model", allow_custom_value=True)
                left_reply = gr.Markdown(container=True, min_height=160)
                left_meta = gr.Markdown()
            with gr.Column():
                right_sdk = gr.Radio(SDKS, value=SDK_OPENAI, label="SDK")
                right_model = gr.Dropdown(MODELS, value=MODELS[min(1, len(MODELS) - 1)], label="Model", allow_custom_value=True)
                right_reply = gr.Markdown(container=True, min_height=160)
                right_meta = gr.Markdown()
        for column_sdk, column_model, column_reply, column_meta in [
                (left_sdk, left_model, left_reply, left_meta),
                (right_sdk, right_model, right_reply, right_meta)]:
            compare_button.click(
                side,
                [column_sdk, column_model, compare_prompt, compare_stream, compare_max_tokens, openai_key, anthropic_key, gemini_key, azure_key],
                [column_reply, column_meta])

if __name__ == "__main__":
    demo.queue().launch(server_name="0.0.0.0", server_port=int(os.environ.get("GRADIO_SERVER_PORT", "7860")))
