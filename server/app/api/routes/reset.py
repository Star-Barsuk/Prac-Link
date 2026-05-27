from fastapi import APIRouter, HTTPException, status
from data_base import Database, MyDatabaseError

router = APIRouter()


@router.post("/reset", status_code=status.HTTP_200_OK)
async def reset_database():
    try:
        async with Database() as db:
            await Database.reset()
        return {"detail": "Database reset successful"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
