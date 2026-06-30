from fastapi import FastAPI, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from urllib.parse import unquote

from api.engine_routes import router as engine_router
from api.security import verify_internal_key
from api.llm_service import generate_response

from dotenv import load_dotenv
import os

load_dotenv()

APP_NAME = os.getenv("APP_NAME")
APP_PORT = os.getenv("APP_PORT")
ENVIRONMENT = os.getenv("ENVIRONMENT")

app = FastAPI()

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global Exception Handler
@app.exception_handler(Exception)
async def global_exception_handler(
        request: Request,
        exc: Exception):

    print(f"Unhandled Exception: {exc}")

    return JSONResponse(
        status_code=500,
        content={
            "status": "FAILED",
            "error": str(exc)
        }
    )

app.include_router(engine_router)

@app.get("/health")
def health():
    return {"status": "UP"}

@app.get("/config")
def config():
    return {
        "app_name": APP_NAME,
        "app_port": APP_PORT,
        "environment": ENVIRONMENT
    }

# Protected Endpoint
@app.get("/chat")
def chat(
        message: str,
        _: None = Depends(verify_internal_key)
):

    # Decode the prompt received from Chatbot Bridge
    message = unquote(unquote(message))

    print("\n========== RECEIVED PROMPT ==========")
    print(message)
    print("=====================================\n")

    # Call the centralized LLM service
    ai_response = generate_response(message)

    return {
        "user_message": message,
        "ai_response": ai_response
    }

# Temporary Test Endpoint
@app.get("/error-test")
def error_test():
    raise Exception("Test Exception")