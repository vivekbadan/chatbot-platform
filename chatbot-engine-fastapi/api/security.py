from typing import Optional
from fastapi import Header, HTTPException

INTERNAL_API_KEY = "chatbot-secret-key"

def verify_internal_key(
        x_internal_key: Optional[str] = Header(None)
):
    if x_internal_key != INTERNAL_API_KEY:
        raise HTTPException(
            status_code=401,
            detail="Invalid Internal API Key"
        )