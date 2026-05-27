from fastapi import APIRouter, Depends, HTTPException, status, Query
from app.schemas.input.practice import PracticeBaseCreate, PracticeBaseUpdate
from typing import Optional
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repos():
    async with Database() as db:
        yield (
            db.get_repository("practice_bases"),
            db.get_repository("practice_registrations"),
            db.get_repository("users"),
            db.get_repository("chats"),
            db.get_repository("chat_members")
        )


@router.post("", status_code=status.HTTP_201_CREATED)
async def create_practice_base(data: PracticeBaseCreate, repos=Depends(get_repos)):
    bases_repo, _, users_repo, chats_repo, members_repo = repos
    try:
        if not await users_repo.fetch_one("by_id", (data.supervisor_id,)):
            raise HTTPException(status_code=404, detail="Supervisor not found")

        base_id = await bases_repo.insert((
            data.name, data.description, data.capacity,
            data.year_id, data.course_id, data.group_id, data.supervisor_id
        ))

        # Создаём/присоединяем чат практики
        chat_name = f"Чат практики: {data.name}"
        chat = await chats_repo.fetch_one("by_name", (chat_name,))
        if not chat:
            chat_id = await chats_repo.insert((chat_name,))
            await members_repo.insert((chat_id, data.supervisor_id))
        else:
            chat_id = chat["id"]
            if not await members_repo.fetch_one("member_by_chat_and_user", (chat_id, data.supervisor_id)):
                await members_repo.insert((chat_id, data.supervisor_id))

        base = await bases_repo.fetch_one("by_id", (base_id,))
        return {**base, "chat_id": chat_id}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/bases")
async def get_practice_bases(
    year_id: int = Query(...),
    course_id: int = Query(...),
    group_id: Optional[int] = Query(None),
    repos=Depends(get_repos)
):
    bases_repo, registrations_repo, _, _, _ = repos
    try:
        bases = await bases_repo.fetch_many("by_year_course_group", (year_id, course_id, group_id, group_id))
        enriched = []
        for base in bases:
            participants = await registrations_repo.fetch_many("participants", (base["id"],))
            enriched.append({
                **base,
                "participants_count": len(participants or []),
                "participants": participants or []
            })
        return {"year_id": year_id, "course_id": course_id, "group_id": group_id, "bases": enriched}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/bases/{base_id}")
async def get_practice_base_details(base_id: int, repos=Depends(get_repos)):
    bases_repo, registrations_repo, _, _, _ = repos
    try:
        base = await bases_repo.fetch_one("by_id", (base_id,))
        if not base:
            raise HTTPException(status_code=404, detail="Practice base not found")

        participants = await registrations_repo.fetch_many("participants", (base_id,))
        return {
            "base": base,
            "participants": participants or [],
            "available_slots": base["capacity"] - len(participants or [])
        }
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{base_id}")
async def update_practice_base(base_id: int, data: PracticeBaseUpdate, repos=Depends(get_repos)):
    bases_repo, _, users_repo, _, _ = repos
    try:
        current = await bases_repo.fetch_one("by_id", (base_id,))
        if not current:
            raise HTTPException(status_code=404, detail="Practice base not found")

        if data.supervisor_id and not await users_repo.fetch_one("by_id", (data.supervisor_id,)):
            raise HTTPException(status_code=404, detail="Supervisor not found")

        name = data.name or current["name"]
        description = data.description or current["description"]
        capacity = data.capacity or current["capacity"]
        supervisor_id = data.supervisor_id or current["supervisor_id"]

        await bases_repo.update((name, description, capacity, supervisor_id, base_id))
        return await bases_repo.fetch_one("by_id", (base_id,))
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{base_id}")
async def delete_practice_base(base_id: int, repos=Depends(get_repos)):
    bases_repo, _, _, chats_repo, _ = repos
    try:
        base = await bases_repo.fetch_one("by_id", (base_id,))
        if not base:
            raise HTTPException(status_code=404, detail="Practice base not found")

        chat_name = f"Чат практики: {base['name']}"
        chat = await chats_repo.fetch_one("by_name", (chat_name,))
        if chat:
            await chats_repo.delete("by_id", (chat["id"],))

        await bases_repo.delete("by_id", (base_id,))
        return {"message": "Practice base deleted"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
