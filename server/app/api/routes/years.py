from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.years import YearCreate, YearUpdate
from app.schemas.output.years import YearOutput, YearsListOutput
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repo():
    async with Database() as db:
        yield db.get_repository("years")


@router.post("", response_model=YearOutput, status_code=status.HTTP_201_CREATED)
async def create_year(data: YearCreate, repo=Depends(get_repo)):
    try:
        if await repo.fetch_one("by_name", (data.name,)):
            raise HTTPException(status_code=400, detail="Year with this name already exists")

        await repo.insert((data.name, data.start_date, data.end_date))
        return YearOutput(**await repo.fetch_one("by_name", (data.name,)))
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("", response_model=YearsListOutput)
async def get_all_years(repo=Depends(get_repo)):
    try:
        years = sorted(await repo.fetch_many("all"), key=lambda x: x["name"], reverse=True)
        return YearsListOutput(years=[YearOutput(**y) for y in years])
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/current", response_model=YearOutput)
async def get_current_year(repo=Depends(get_repo)):
    try:
        year = await repo.fetch_one("current")
        if not year:
            raise HTTPException(status_code=404, detail="No current year found")
        return YearOutput(**year)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{year_id}", response_model=YearOutput)
async def update_year(year_id: int, data: YearUpdate, repo=Depends(get_repo)):
    try:
        current = await repo.fetch_one("by_id", (year_id,))
        if not current:
            raise HTTPException(status_code=404, detail="Year not found")

        name = data.name or current["name"]
        start_date = data.start_date or current["start_date"]
        end_date = data.end_date or current["end_date"]

        await repo.update((name, start_date, end_date, year_id))
        return YearOutput(**await repo.fetch_one("by_id", (year_id,)))
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{year_id}")
async def delete_year(year_id: int, repo=Depends(get_repo)):
    try:
        if not await repo.fetch_one("by_id", (year_id,)):
            raise HTTPException(status_code=404, detail="Year not found")
        await repo.delete("by_id", (year_id,))
        return {"message": "Year deleted"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
