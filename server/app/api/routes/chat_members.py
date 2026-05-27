from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.chat_members import ChatMemberCreate
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repo():
    async with Database() as db:
        yield db.get_repository("chat_members")


@router.post("", status_code=status.HTTP_201_CREATED)
async def add_member(data: ChatMemberCreate, repo=Depends(get_repo)):
    try:
        if await repo.fetch_one("member_by_chat_and_user", (data.chat_id, data.user_id)):
            raise HTTPException(status_code=400, detail="User already in this chat")

        await repo.insert((data.chat_id, data.user_id))
        return {"message": "User added to chat"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/chat/{chat_id}")
async def get_chat_members(chat_id: int, repo=Depends(get_repo)):
    try:
        return {"members": await repo.fetch_many("members_by_chat", (chat_id,))}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/user/{user_id}")
async def get_user_chats(user_id: int, repo=Depends(get_repo)):
    try:
        return {"chats": await repo.fetch_many("chats_by_user", (user_id,))}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("")
async def remove_member(data: ChatMemberCreate, repo=Depends(get_repo)):
    try:
        if not await repo.fetch_one("member_by_chat_and_user", (data.chat_id, data.user_id)):
            raise HTTPException(status_code=404, detail="User not found in this chat")

        await repo.delete("by_chat_and_user", (data.chat_id, data.user_id))
        return {"message": "User removed from chat"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
