import pytest
from pydantic import ValidationError

from models.chat_schema import ChatRequest


def test_valid_chat_request():
    request = ChatRequest(
        message="Hello",
        organization_id=1
    )

    assert request.message == "Hello"
    assert request.organization_id == 1


def test_invalid_organization_id():
    with pytest.raises(ValidationError):
        ChatRequest(
            message="Hello",
            organization_id=0
        )


def test_empty_message():
    with pytest.raises(ValidationError):
        ChatRequest(
            message="",
            organization_id=1
        )