import os
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

client = Groq(
    api_key=os.getenv("GROQ_API_KEY")
)

def get_ai_response(message: str):

    print("\n========== PROMPT SENT TO GROQ ==========")
    print(message)
    print("=========================================\n")

    chat_completion = client.chat.completions.create(
        model="llama-3.1-8b-instant",
        messages=[
            {
                "role": "user",
                "content": message
            }
        ],
        temperature=0.7,
        max_tokens=200
    )

    ai_response = chat_completion.choices[0].message.content

    print("\n========== GROQ RESPONSE ==========")
    print(ai_response)
    print("==================================\n")

    return ai_response

