from fastapi import APIRouter, Depends, HTTPException, status, Query
from app.schemas.input.messages import MessageCreate, MessageUpdate
from app.schemas.output.messages import (
    MessageCreateResponse,
    MessageDeleteResponse,
    ChatMessagesOutput,
    MessageOutput,
    MessageSenderOutput
)
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repos():
    async with Database() as db:
        yield (
            db.get_repository("messages"),
            db.get_repository("chats"),
            db.get_repository("users"),
            db.get_repository("chat_members")
        )


@router.post("", response_model=MessageCreateResponse, status_code=status.HTTP_201_CREATED)
async def send_message(data: MessageCreate, repos=Depends(get_repos)):
    messages_repo, chats_repo, users_repo, members_repo = repos
    try:
        if not await chats_repo.fetch_one("by_id", (data.chat_id,)):
            raise HTTPException(status_code=404, detail="Chat not found")
        if not await members_repo.fetch_one("member_by_chat_and_user", (data.chat_id, data.sender_id)):
            raise HTTPException(status_code=403, detail="User is not a member of this chat")

        sender = await users_repo.fetch_one("by_id", (data.sender_id,))
        message_id = await messages_repo.insert((data.chat_id, data.sender_id, data.content.strip()))

        message = await messages_repo.fetch_one("by_id", (message_id,))
        return MessageCreateResponse(
            id=message["id"],
            chat_id=message["chat_id"],
            content=message["content"],
            sent_at=message["sent_at"].isoformat(),
            sender=MessageSenderOutput(
                id=sender["id"],
                username=sender["username"],
                email=sender["email"]
            )
        )
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/{chat_id}", response_model=ChatMessagesOutput)
async def get_messages(chat_id: int, repos=Depends(get_repos)):
    messages_repo, _, _, _ = repos
    try:
        messages = await messages_repo.fetch_many("by_chat", (chat_id,))
        result = [
            MessageOutput(
                id=m["id"],
                chat_id=chat_id,
                content=m["content"],
                sent_at=m["sent_at"].isoformat(),
                sender=MessageSenderOutput(
                    id=m["sender_id"],
                    username=m["sender_username"],
                    email=m.get("sender_email")
                )
            ) for m in messages
        ]
        return ChatMessagesOutput(chat_id=chat_id, count=len(result), messages=result)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/{chat_id}/new", response_model=ChatMessagesOutput)
async def get_new_messages(chat_id: int, last_id: int = Query(0), repos=Depends(get_repos)):
    messages_repo, _, _, _ = repos
    try:
        messages = await messages_repo.fetch_many("new_messages", (chat_id, last_id))
        result = [
            MessageOutput(
                id=m["id"],
                chat_id=chat_id,
                content=m["content"],
                sent_at=m["sent_at"].isoformat(),
                sender=MessageSenderOutput(
                    id=m["sender_id"],
                    username=m["sender_username"],
                    email=m.get("sender_email")
                )
            ) for m in messages
        ]
        return ChatMessagesOutput(chat_id=chat_id, count=len(result), messages=result)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{message_id}", response_model=MessageOutput)
async def update_message(message_id: int, data: MessageUpdate, repos=Depends(get_repos)):
    messages_repo, _, users_repo, _ = repos
    try:
        current = await messages_repo.fetch_one("by_id", (message_id,))
        if not current:
            raise HTTPException(status_code=404, detail="Message not found")

        content = data.content or current["content"]
        await messages_repo.update((content, message_id))

        updated = await messages_repo.fetch_one("by_id", (message_id,))
        sender = await users_repo.fetch_one("by_id", (updated["sender_id"],))

        return MessageOutput(
            id=updated["id"],
            chat_id=updated["chat_id"],
            content=updated["content"],
            sent_at=updated["sent_at"].isoformat(),
            sender=MessageSenderOutput(
                id=sender["id"],
                username=sender["username"],
                email=sender["email"]
            )
        )
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{message_id}", response_model=MessageDeleteResponse)
async def delete_message(message_id: int, repos=Depends(get_repos)):
    messages_repo, _, _, _ = repos
    try:
        if not await messages_repo.fetch_one("by_id", (message_id,)):
            raise HTTPException(status_code=404, detail="Message not found")
        await messages_repo.delete("by_id", (message_id,))
        return MessageDeleteResponse(message="Message deleted")
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
