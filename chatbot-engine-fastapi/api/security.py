from fastapi import Header, HTTPException

def verify_internal_key(x_internal_key: str = Header(...)):

    print("Validating internal API key...")

    if x_internal_key != "chatbot-secret-key":
        raise HTTPException(
            status_code=403,
            detail="Invalid Internal API Key"
        )

    print("Internal API key validated successfully")