from unittest.mock import patch

from api.llm_service import generate_response


@patch("api.llm_service.get_ai_response")
def test_generate_response(mock_get_ai_response):

    # Mock the Groq API response
    mock_get_ai_response.return_value = {
        "response": "Hello from mocked LLM!",
        "token_count": 42
    }

    prompt = "Hello AI"

    result = generate_response(prompt)

    # Verify the returned response
    assert result["response"] == "Hello from mocked LLM!"
    assert result["token_count"] == 42

    # Verify Groq service was called once
    mock_get_ai_response.assert_called_once_with(prompt)