from typing import List, Optional
from .._infrastructure.database_wrapper import DatabaseWrapper
from .database_builder import DatabaseBuilder
from .._shared.messages.msg_database import MsgDataBase
from .._shared.exceptions import MyDatabaseError

class Database:
    _instance: Optional[DatabaseWrapper] = None

    def __init__(self) -> None:
        self._db: Optional[DatabaseWrapper] = None

    @classmethod
    async def init(cls, config_path: str = "config/database"):
        if cls._instance is None:
            builder = DatabaseBuilder(config_path)
            cls._instance = await builder.build()
            if cls._instance is None:
                MsgDataBase.Failure.instance_build_failed()
                raise MyDatabaseError("Database build failed.")

    async def __aenter__(self) -> DatabaseWrapper:
        if self._db is None:
            if Database._instance is None:
                MsgDataBase.Failure.instance_build_failed()
                raise MyDatabaseError("Database not initialized")
            self._db = Database._instance
        return self._db

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        pass

    @classmethod
    async def close_instance(cls):
        if cls._instance is not None:
            await cls._instance.close()
            cls._instance = None

    @classmethod
    async def create(cls):
        if cls._instance is None:
            MsgDataBase.Failure.instance_build_failed()
            raise MyDatabaseError("Database not initialized")

        queries_dict = cls._instance.queries_dict
        table_names: List[str] = list(queries_dict.keys())

        for table_name in table_names:
            repo = cls._instance.get_repository(table_name)
            if repo is not None:
                await repo.create()

    @classmethod
    async def drop(cls):
        if cls._instance is None:
            MsgDataBase.Failure.instance_build_failed()
            raise MyDatabaseError("Database not initialized")

        queries_dict = cls._instance.queries_dict
        table_names: List[str] = list(queries_dict.keys())

        for table_name in reversed(table_names):
            repo = cls._instance.get_repository(table_name)
            if repo is not None:
                await repo.drop()

    @classmethod
    async def reset(cls):
        if cls._instance is None:
            MsgDataBase.Failure.instance_build_failed()
            raise MyDatabaseError("Database not initialized")

        queries_dict = cls._instance.queries_dict
        table_names: List[str] = list(queries_dict.keys())

        for table_name in reversed(table_names):
            repo = cls._instance.get_repository(table_name)
            if repo is not None:
                await repo.drop()

        for table_name in table_names:
            repo = cls._instance.get_repository(table_name)
            if repo is not None:
                await repo.create()
