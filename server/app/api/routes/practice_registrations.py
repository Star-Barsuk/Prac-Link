from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.practice_registrations import RegistrationRequest
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repos():
    async with Database() as db:
        yield (
            db.get_repository("practice_registrations"),
            db.get_repository("practice_bases"),
            db.get_repository("users"),
            db.get_repository("chats"),
            db.get_repository("chat_members")
        )


@router.post("/register")
async def register_for_practice(data: RegistrationRequest, repos=Depends(get_repos)):
    reg_repo, bases_repo, users_repo, chats_repo, members_repo = repos
    try:
        base = await bases_repo.fetch_one("by_id", (data.base_id,))
        if not base:
            raise HTTPException(status_code=404, detail="Practice base not found")
        if not await users_repo.fetch_one("by_id", (data.user_id,)):
            raise HTTPException(status_code=404, detail="User not found")

        user_regs = await reg_repo.fetch_many("user_registrations", (data.user_id,))
        user_regs = user_regs or []
        if any(r.get("id") == data.base_id for r in user_regs):
            raise HTTPException(status_code=400, detail="Already registered")

        participants = await reg_repo.fetch_many("participants", (data.base_id,))
        participants = participants or []
        if len(participants) >= base["capacity"]:
            raise HTTPException(status_code=400, detail="No available slots")

        await reg_repo.insert((data.user_id, data.base_id))

        chat_name = f"Чат практики: {base['name']}"
        chat = await chats_repo.fetch_one("by_name", (chat_name,))
        if chat and not await members_repo.fetch_one("member_by_chat_and_user", (chat["id"], data.user_id)):
            await members_repo.insert((chat["id"], data.user_id))

        final_participants = await reg_repo.fetch_many("participants", (data.base_id,))
        return {
            "message": "Successfully registered",
            "available_slots": base["capacity"] - len(final_participants or [])
        }
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.post("/unregister")
async def unregister_from_practice(data: RegistrationRequest, repos=Depends(get_repos)):
    reg_repo, bases_repo, users_repo, chats_repo, members_repo = repos
    try:
        base = await bases_repo.fetch_one("by_id", (data.base_id,))
        if not base:
            raise HTTPException(status_code=404, detail="Practice base not found")
        if not await users_repo.fetch_one("by_id", (data.user_id,)):
            raise HTTPException(status_code=404, detail="User not found")

        user_regs = await reg_repo.fetch_many("user_registrations", (data.user_id,))
        if not any(r["id"] == data.base_id for r in user_regs):
            raise HTTPException(status_code=400, detail="Not registered")

        await reg_repo.delete("by_user_and_base", (data.user_id, data.base_id))

        chat = await chats_repo.fetch_one("by_name", (f"Чат практики: {base['name']}",))
        if chat:
            await members_repo.delete("by_chat_and_user", (chat["id"], data.user_id))

        final = await reg_repo.fetch_many("participants", (data.base_id,))
        return {
            "message": "Successfully unregistered",
            "available_slots": base["capacity"] - len(final or [])
        }
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/user/{user_id}")
async def get_user_practice_registrations(user_id: int, repos=Depends(get_repos)):
    reg_repo, bases_repo, users_repo, _, _ = repos
    try:
        if not await users_repo.fetch_one("by_id", (user_id,)):
            raise HTTPException(status_code=404, detail="User not found")

        registrations = await reg_repo.fetch_many("user_registrations", (user_id,))
        bases = []
        for r in registrations:
            base = await bases_repo.fetch_one("by_id", (r["id"],))
            if base:
                participants = await reg_repo.fetch_many("participants", (base["id"],))
                bases.append({**base, "participants_count": len(participants or [])})

        return {"user_id": user_id, "practice_bases": bases}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/base/{base_id}")
async def get_practice_participants(base_id: int, repos=Depends(get_repos)):
    reg_repo, bases_repo, _, _, _ = repos
    try:
        if not await bases_repo.fetch_one("by_id", (base_id,)):
            raise HTTPException(status_code=404, detail="Practice base not found")
        return {
            "base_id": base_id,
            "participants": await reg_repo.fetch_many("participants", (base_id,)) or []
        }
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
