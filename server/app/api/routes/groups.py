from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.groups import GroupCreate, GroupUpdate
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repo():
    async with Database() as db:
        yield db.get_repository("groups")


@router.post("", status_code=status.HTTP_201_CREATED)
async def create_group(data: GroupCreate, repo=Depends(get_repo)):
    try:
        async with Database() as db:
            if not await db.get_repository("years").fetch_one("by_id", (data.year_id,)):
                raise HTTPException(status_code=404, detail="Year not found")
            if not await db.get_repository("courses").fetch_one("by_id", (data.course_id,)):
                raise HTTPException(status_code=404, detail="Course not found")

        if await repo.fetch_one("by_name_year_course", (data.name, data.year_id, data.course_id)):
            raise HTTPException(status_code=400, detail="Group with this name already exists for selected year and course")

        group_id = await repo.insert((data.name, data.year_id, data.course_id))
        return await repo.fetch_one("by_id", (group_id,))
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("")
async def get_groups(year_id: int, course_id: int, repo=Depends(get_repo)):
    try:
        return {"groups": await repo.fetch_many("by_year_course", (year_id, course_id))}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{group_id}")
async def update_group(group_id: int, data: GroupUpdate, repo=Depends(get_repo)):
    try:
        current = await repo.fetch_one("by_id", (group_id,))
        if not current:
            raise HTTPException(status_code=404, detail="Group not found")

        name = data.name or current["name"]
        year_id = data.year_id or current["year_id"]
        course_id = data.course_id or current["course_id"]

        if data.name:
            groups = await repo.fetch_many("by_year_course", (year_id, course_id))
            if any(g["name"] == name and g["id"] != group_id for g in groups):
                raise HTTPException(status_code=400, detail="Group with this name already exists")

        await repo.update((name, year_id, course_id, group_id))
        return await repo.fetch_one("by_id", (group_id,))
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{group_id}")
async def delete_group(group_id: int, repo=Depends(get_repo)):
    try:
        if not await repo.fetch_one("by_id", (group_id,)):
            raise HTTPException(status_code=404, detail="Group not found")
        await repo.delete("by_id", (group_id,))
        return {"message": "Group deleted"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
