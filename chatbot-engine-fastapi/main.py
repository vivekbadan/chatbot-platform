from fastapi import FastAPI, Depends, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from dotenv import load_dotenv
from api.engine_routes import router as engine_router
from api.security import verify_internal_key
import os

load_dotenv()

APP_NAME = os.getenv("APP_NAME")
APP_PORT = os.getenv("APP_PORT")
ENVIRONMENT = os.getenv("ENVIRONMENT")

app = FastAPI()

# CORS Configuration
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

    return JSONResponse(
        status_code=500,
        content={
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
    return {
        "response": f"You said: {message}"
    }

# Temporary Test Endpoint
@app.get("/error-test")
def error_test():
    raise Exception("Test Exception")