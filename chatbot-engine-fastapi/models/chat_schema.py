from pydantic import BaseModel, Field
from typing import Optional


class ChatRequest(BaseModel):
    message: str = Field(
        ...,
        min_length=1,
        max_length=1000,
        description="User message"
    )

    organization_id: int = Field(
        ...,
        gt=0,
        description="Organization ID"
    )


class ChatResponse(BaseModel):
    response: str


class EngineHealthResponse(BaseModel):
    status: str
    version: str