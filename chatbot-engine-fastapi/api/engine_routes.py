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

    prompt = f"""
Organization ID: {request.organization_id}

User Message:
{request.message}
"""

    ai_response = generate_response(prompt)

    return ChatResponse(
        response=ai_response
    )
@router.post("/embed")
def embed():
    return {
        "embedding": [0.1, 0.2, 0.3]
    }