import os
import sys

from gateway import SDK_ANTHROPIC, SDK_BEDROCK, SDK_GEMINI, SDK_OPENAI, chat

SDK_NAMES = {"openai": SDK_OPENAI, "anthropic": SDK_ANTHROPIC, "gemini": SDK_GEMINI, "bedrock": SDK_BEDROCK}


def flag(name):
    return os.environ.get(name, "").lower() in ("1", "true", "yes")


def main():
    sdk = SDK_NAMES.get(os.environ.get("SDK", "openai").lower(), SDK_OPENAI)
    model = os.environ.get("MODEL", "gpt-4o-mini")
    tools = flag("TOOLS")
    default_prompt = "What is the weather in Paris?" if tools else "In one sentence, what is an LLM gateway?"
    prompt = os.environ.get("PROMPT", default_prompt)
    max_tokens = int(os.environ.get("MAX_TOKENS", "512"))

    printed, meta = 0, ""
    for text, meta in chat(sdk, model, prompt, flag("STREAM"), tools, max_tokens):
        sys.stdout.write(text[printed:].replace("`", ""))
        sys.stdout.flush()
        printed = len(text)

    print()
    print(meta.replace("**", "").replace("  \n", "\n").replace("`", ""))
    return 1 if "Error" in meta else 0


if __name__ == "__main__":
    sys.exit(main())
