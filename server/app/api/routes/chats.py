from fastapi import APIRouter, Depends, HTTPException, status, Query
from app.schemas.input.chats import ChatCreate, ChatUpdate
from app.schemas.output.chats import ChatOutput, UserChatsOutput, ChatCreateResponse, ChatDeleteResponse, ChatMemberOutput
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repos():
    async with Database() as db:
        yield (
            db.get_repository("chats"),
            db.get_repository("chat_members"),
            db.get_repository("users")
        )


@router.post("", response_model=ChatCreateResponse, status_code=status.HTTP_201_CREATED)
async def create_chat(data: ChatCreate, repos=Depends(get_repos)):
    chats_repo, members_repo, users_repo = repos
    try:
        chat_id = await chats_repo.insert((data.name,))

        for user_id in data.user_ids:
            if not await users_repo.fetch_one("by_id", (user_id,)):
                raise HTTPException(status_code=404, detail=f"User {user_id} not found")
            await members_repo.insert((chat_id, user_id))

        return ChatCreateResponse(message="Chat created", chat_id=chat_id)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/all", response_model=list[ChatOutput])
async def get_all_chats(repos=Depends(get_repos)):
    chats_repo, members_repo, _ = repos
    try:
        result = []
        for chat in await chats_repo.fetch_many("all"):
            members = await members_repo.fetch_many("members_by_chat", (chat["id"],))
            result.append(ChatOutput(
                chat_id=chat["id"],
                name=chat["name"],
                created_at=chat["created_at"],
                members=[ChatMemberOutput(**m) for m in members]
            ))
        return result
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("", response_model=UserChatsOutput)
async def get_user_chats(user_id: int = Query(...), repos=Depends(get_repos)):
    chats_repo, members_repo, _ = repos
    try:
        chat_rows = await members_repo.fetch_many("chats_by_user", (user_id,))
        chats = []
        for row in chat_rows:
            chat = await chats_repo.fetch_one("by_id", (row["chat_id"],))
            if not chat:
                continue
            members = await members_repo.fetch_many("members_by_chat", (chat["id"],))
            chats.append(ChatOutput(
                chat_id=chat["id"],
                name=chat["name"],
                created_at=chat["created_at"],
                members=[ChatMemberOutput(**m) for m in members]
            ))
        return UserChatsOutput(user_id=user_id, chats=chats)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{chat_id}", response_model=ChatOutput)
async def update_chat(chat_id: int, data: ChatUpdate, repos=Depends(get_repos)):
    chats_repo, members_repo, _ = repos
    try:
        current = await chats_repo.fetch_one("by_id", (chat_id,))
        if not current:
            raise HTTPException(status_code=404, detail="Chat not found")

        name = data.name or current["name"]
        if data.name:
            existing = await chats_repo.fetch_one("by_name", (name,))
            if existing and existing["id"] != chat_id:
                raise HTTPException(status_code=400, detail="Chat with this name already exists")

        await chats_repo.update((name, chat_id))
        updated = await chats_repo.fetch_one("by_id", (chat_id,))
        members = await members_repo.fetch_many("members_by_chat", (chat_id,))
        return ChatOutput(
            chat_id=updated["id"],
            name=updated["name"],
            created_at=updated["created_at"],
            members=[ChatMemberOutput(**m) for m in members]
        )
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{chat_id}", response_model=ChatDeleteResponse)
async def delete_chat(chat_id: int, repos=Depends(get_repos)):
    chats_repo, _, _ = repos
    try:
        if not await chats_repo.fetch_one("by_id", (chat_id,)):
            raise HTTPException(status_code=404, detail="Chat not found")
        await chats_repo.delete("by_id", (chat_id,))
        return ChatDeleteResponse(message="Chat deleted")
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.post("/{chat_id}/members", response_model=ChatCreateResponse)
async def add_chat_member(chat_id: int, user_id: int = Query(...), repos=Depends(get_repos)):
    chats_repo, members_repo, users_repo = repos
    try:
        if not await chats_repo.fetch_one("by_id", (chat_id,)):
            raise HTTPException(status_code=404, detail="Chat not found")
        if not await users_repo.fetch_one("by_id", (user_id,)):
            raise HTTPException(status_code=404, detail="User not found")
        if await members_repo.fetch_one("member_by_chat_and_user", (chat_id, user_id)):
            raise HTTPException(status_code=400, detail="User already in this chat")

        await members_repo.insert((chat_id, user_id))
        return ChatCreateResponse(message="User added to chat", chat_id=chat_id)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{chat_id}/members")
async def remove_chat_member(chat_id: int, user_id: int = Query(...), repos=Depends(get_repos)):
    chats_repo, members_repo, _ = repos
    try:
        if not await chats_repo.fetch_one("by_id", (chat_id,)):
            raise HTTPException(status_code=404, detail="Chat not found")
        if not await members_repo.fetch_one("member_by_chat_and_user", (chat_id, user_id)):
            raise HTTPException(status_code=404, detail="User not found in this chat")

        await members_repo.delete("by_chat_and_user", (chat_id, user_id))
        return {"message": "User removed from chat"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
