from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.courses import CourseCreate, CourseUpdate
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repo():
    async with Database() as db:
        yield db.get_repository("courses")


@router.post("", status_code=status.HTTP_201_CREATED)
async def create_course(data: CourseCreate, repo=Depends(get_repo)):
    try:
        if await repo.fetch_one("by_name", (data.name,)):
            raise HTTPException(status_code=400, detail="Course with this name already exists")

        await repo.insert((data.name,))
        return await repo.fetch_one("by_name", (data.name,))
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("")
async def get_all_courses(repo=Depends(get_repo)):
    try:
        return {"courses": await repo.fetch_many("all")}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{course_id}")
async def update_course(course_id: int, data: CourseUpdate, repo=Depends(get_repo)):
    try:
        current = await repo.fetch_one("by_id", (course_id,))
        if not current:
            raise HTTPException(status_code=404, detail="Course not found")

        if data.name and data.name != current["name"]:
            if await repo.fetch_one("by_name", (data.name,)):
                raise HTTPException(status_code=400, detail="Course with this name already exists")

        name = data.name or current["name"]
        await repo.update((name, course_id))
        return await repo.fetch_one("by_id", (course_id,))
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{course_id}")
async def delete_course(course_id: int, repo=Depends(get_repo)):
    try:
        if not await repo.fetch_one("by_id", (course_id,)):
            raise HTTPException(status_code=404, detail="Course not found")
        await repo.delete("by_id", (course_id,))
        return {"message": "Course deleted"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
