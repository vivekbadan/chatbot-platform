from fastapi import APIRouter

from models.chat_schema import (
    ChatRequest,
    ChatResponse,
    EngineHealthResponse
)

router = APIRouter(
    prefix="/engine",
    tags=["Engine"]
)


@router.get(
    "/health",
    response_model=EngineHealthResponse
)
def health():
    return EngineHealthResponse(
        status="UP",
        version="1.0.0"
    )


@router.post(
    "/chat",
    response_model=ChatResponse
)
def chat(request: ChatRequest):
    return ChatResponse(
        response=f"Mock response for: {request.message}"
    )


@router.post("/embed")
def embed():
    return {
        "embedding": [0.1, 0.2, 0.3]
    }