from fastapi import FastAPI

app = FastAPI()

@app.get("/")
def root():
    return {"message": "Chatbot Engine Running"}

@app.get("/health")
def health():
    return {"status": "UP"}

@app.get("/chat")
def chat(message: str):
    return {"response": f"You said: {message}"}