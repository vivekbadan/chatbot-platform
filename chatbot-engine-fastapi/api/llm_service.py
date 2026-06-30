import os

from api.groq_service import get_ai_response

LLM_PROVIDER = os.getenv("LLM_PROVIDER", "groq").lower()


def generate_response(prompt: str) -> str:
    """
    Central LLM service.

    Supported providers:
    - groq
    - gemini (future)
    - openai (future)
    """

    if LLM_PROVIDER == "groq":
        return get_ai_response(prompt)

    raise ValueError(f"Unsupported LLM Provider: {LLM_PROVIDER}")