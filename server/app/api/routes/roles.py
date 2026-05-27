from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.roles import RoleCreate, RoleUpdate
from app.schemas.output.roles import RoleOutput
from data_base import Database, MyDatabaseError

router = APIRouter()


async def get_repo():
    async with Database() as db:
        yield db.get_repository("roles")


@router.post("", response_model=RoleOutput, status_code=status.HTTP_201_CREATED)
async def create_role(data: RoleCreate, repo=Depends(get_repo)):
    try:
        if await repo.fetch_one("by_name", (data.name,)):
            raise HTTPException(status_code=400, detail="Role with this name already exists")

        await repo.insert((data.name, data.description))
        role = await repo.fetch_one("by_name", (data.name,))
        return RoleOutput(**role)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("", response_model=list[RoleOutput])
async def get_all_roles(repo=Depends(get_repo)):
    try:
        roles = await repo.fetch_many("all")
        return [RoleOutput(**r) for r in roles]
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/{role_id}", response_model=RoleOutput)
async def get_role(role_id: int, repo=Depends(get_repo)):
    try:
        role = await repo.fetch_one("by_id", (role_id,))
        if not role:
            raise HTTPException(status_code=404, detail="Role not found")
        return RoleOutput(**role)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{role_id}", response_model=RoleOutput)
async def update_role(role_id: int, data: RoleUpdate, repo=Depends(get_repo)):
    try:
        current = await repo.fetch_one("by_id", (role_id,))
        if not current:
            raise HTTPException(status_code=404, detail="Role not found")

        name = data.name or current["name"]
        description = data.description if data.description is not None else current["description"]

        if data.name and data.name != current["name"]:
            if await repo.fetch_one("by_name", (name,)):
                raise HTTPException(status_code=400, detail="Role name already taken")

        await repo.update((name, description, role_id))
        updated = await repo.fetch_one("by_id", (role_id,))
        return RoleOutput(**updated)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.delete("/{role_id}")
async def delete_role(role_id: int, repo=Depends(get_repo)):
    try:
        if not await repo.fetch_one("by_id", (role_id,)):
            raise HTTPException(status_code=404, detail="Role not found")
        await repo.delete("by_id", (role_id,))
        return {"message": "Role deleted"}
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
