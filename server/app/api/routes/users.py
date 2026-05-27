from fastapi import APIRouter, Depends, HTTPException, status
from app.schemas.input.users import UserCreate, UserLogin, UserUpdate
from app.schemas.output.users import UserOutput
from data_base import Database, MyDatabaseError
import hashlib
import secrets

router = APIRouter()


def hash_password(password: str, salt: str = None) -> tuple[str, str]:
    if not salt:
        salt = secrets.token_hex(16)
    hashed = hashlib.sha256((password + salt).encode()).hexdigest()
    return hashed, salt


async def get_user_repo():
    async with Database() as db:
        yield db.get_repository("users")


async def get_roles_repo():
    async with Database() as db:
        yield db.get_repository("roles")


async def get_user_roles_repo():
    async with Database() as db:
        yield db.get_repository("user_roles")


async def add_to_general_chat(user_id: int):
    async with Database() as db:
        chats_repo = db.get_repository("chats")
        members_repo = db.get_repository("chat_members")
        chat = await chats_repo.fetch_one("by_name", ("Общий чат",))
        chat_id = chat["id"] if chat else await chats_repo.insert(("Общий чат",))
        if not await members_repo.fetch_one("member_by_chat_and_user", (chat_id, user_id)):
            await members_repo.insert((chat_id, user_id))


@router.post("/register", response_model=UserOutput, status_code=status.HTTP_201_CREATED)
async def register_user(
    data: UserCreate,
    user_repo=Depends(get_user_repo),
    roles_repo=Depends(get_roles_repo),
    user_roles_repo=Depends(get_user_roles_repo)
):
    try:
        if await user_repo.fetch_one("by_username", (data.username,)):
            raise HTTPException(status_code=400, detail="Username already exists")
        if await user_repo.fetch_one("by_email", (data.email,)):
            raise HTTPException(status_code=400, detail="Email already registered")

        hashed, salt = hash_password(data.password)
        password_hash = f"{salt}:{hashed}"

        await user_repo.insert((data.username, data.email, password_hash))
        user = await user_repo.fetch_one("by_username", (data.username,))

        for role_name in data.roles:
            role = await roles_repo.fetch_one("by_name", (role_name,))
            if not role:
                raise HTTPException(status_code=400, detail=f"Role '{role_name}' not found")
            await user_roles_repo.insert((user["id"], role["id"]))

        await add_to_general_chat(user["id"])

        roles = await user_roles_repo.fetch_many("role_names_by_user", (user["id"],))
        user["roles"] = [r["name"] for r in roles] if roles else []

        return UserOutput(**user)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.post("/login", response_model=UserOutput)
async def login_user(
    data: UserLogin,
    user_repo=Depends(get_user_repo),
    user_roles_repo=Depends(get_user_roles_repo)
):
    try:
        user = await user_repo.fetch_one("by_email", (data.email,))
        if not user:
            raise HTTPException(status_code=401, detail="Invalid email or password")

        salt, hashed = user["password_hash"].split(":")
        if hashlib.sha256((data.password + salt).encode()).hexdigest() != hashed:
            raise HTTPException(status_code=401, detail="Invalid email or password")

        roles = await user_roles_repo.fetch_many("role_names_by_user", (user["id"],))
        user["roles"] = [r["name"] for r in roles] if roles else []

        return UserOutput(**user)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.put("/{user_id}", response_model=UserOutput)
async def update_user(
    user_id: int,
    data: UserUpdate,
    user_repo=Depends(get_user_repo),
    user_roles_repo=Depends(get_user_roles_repo),
    roles_repo=Depends(get_roles_repo)
):
    try:
        current = await user_repo.fetch_one("by_id", (user_id,))
        if not current:
            raise HTTPException(status_code=404, detail="User not found")

        username = data.username or current["username"]
        email = data.email or current["email"]
        password_hash = current["password_hash"]

        if data.password:
            salt = secrets.token_hex(16)
            hashed, _ = hash_password(data.password, salt)
            password_hash = f"{salt}:{hashed}"

        await user_repo.update((username, email, password_hash, user_id))

        if data.roles is not None:
            await user_roles_repo.delete("by_user", (user_id,))
            for role_name in data.roles:
                role = await roles_repo.fetch_one("by_name", (role_name,))
                if not role:
                    raise HTTPException(status_code=400, detail=f"Role '{role_name}' not found")
                await user_roles_repo.insert((user_id, role["id"]))

        updated = await user_repo.fetch_one("by_id", (user_id,))
        roles = await user_roles_repo.fetch_many("role_names_by_user", (user_id,))
        updated["roles"] = [r["name"] for r in roles] if roles else []

        return UserOutput(**updated)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("", response_model=list[UserOutput])
async def get_all_users(user_repo=Depends(get_user_repo), user_roles_repo=Depends(get_user_roles_repo)):
    try:
        users = await user_repo.fetch_many("all")
        result = []
        for user in users:
            roles = await user_roles_repo.fetch_many("role_names_by_user", (user["id"],))
            user["roles"] = [r["name"] for r in roles] if roles else []
            result.append(UserOutput(**user))
        return result
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/{user_id}", response_model=UserOutput)
async def get_user(user_id: int, user_repo=Depends(get_user_repo), user_roles_repo=Depends(get_user_roles_repo)):
    try:
        user = await user_repo.fetch_one("by_id", (user_id,))
        if not user:
            raise HTTPException(status_code=404, detail="User not found")

        roles = await user_roles_repo.fetch_many("role_names_by_user", (user_id,))
        user["roles"] = [r["name"] for r in roles] if roles else []
        return UserOutput(**user)
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")


@router.get("/exclude/{exclude_id}", response_model=list[UserOutput])
async def get_users_exclude(
    exclude_id: int,
    user_repo=Depends(get_user_repo),
    user_roles_repo=Depends(get_user_roles_repo)
):
    try:
        users = await user_repo.fetch_many("all_except", (exclude_id,))
        result = []
        for user in users:
            roles = await user_roles_repo.fetch_many("role_names_by_user", (user["id"],))
            user["roles"] = [r["name"] for r in roles] if roles else []
            result.append(UserOutput(**user))
        return result
    except MyDatabaseError as e:
        raise HTTPException(status_code=500, detail=f"Database error: {e}")
