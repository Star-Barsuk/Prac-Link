from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.student_groups import StudentGroupCreate
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repo():
    async with Database() as db:
        yield db.get_repository("student_groups")


@router.post("", status_code=status.HTTP_201_CREATED)
async def add_student_to_group(data: StudentGroupCreate, repo=Depends(get_repo)):
    try:
        if await repo.fetch_one("by_student_and_group", (data.student_id, data.group_id)):
            raise HTTPException(status_code=400, detail="Student already in this group")

        await repo.insert((data.student_id, data.group_id))
        return {"message": "Student added to group"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/group/{group_id}")
async def get_group_students(group_id: int, repo=Depends(get_repo)):
    try:
        return {"students": await repo.fetch_many("by_group", (group_id,))}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/student/{student_id}")
async def get_student_groups(student_id: int, repo=Depends(get_repo)):
    try:
        return {"groups": await repo.fetch_many("by_student", (student_id,))}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("")
async def remove_student_from_group(data: StudentGroupCreate, repo=Depends(get_repo)):
    try:
        if not await repo.fetch_one("by_student_and_group", (data.student_id, data.group_id)):
            raise HTTPException(status_code=404, detail="Student not found in this group")

        await repo.delete("by_student_and_group", (data.student_id, data.group_id))
        return {"message": "Student removed from group"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
