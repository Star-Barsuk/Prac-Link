from typing import Dict, Optional
from .base_aiorepository import BaseAioRepository
from .aiomysql.aiomysql_connection import AioMySQLConnection
from .._shared.messages.msg_database import MsgDataBase

class DatabaseWrapper:
    def __init__(self, connection: AioMySQLConnection, queries_dict: Dict[str, dict]) -> None:
        self._connection = connection
        self._queries_dict = queries_dict
        self._repositories: Dict[str, BaseAioRepository] = {}

    def get_repository(self, table_name: str) -> Optional[BaseAioRepository]:
        if table_name not in self._repositories:
            queries = self._queries_dict.get(table_name)
            if not queries:
                MsgDataBase.Failure.query_miss_failed(table_name)
                return None
            self._repositories[table_name] = BaseAioRepository(
                pool=self._connection.pool,
                queries=queries,
                table_name=table_name,
            )
        return self._repositories[table_name]

    @property
    def is_connected(self) -> bool:
        return self._connection.is_connected

    async def close(self) -> None:
        await self._connection.close()

    async def __aenter__(self) -> "AsyncDatabaseClient":
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb) -> None:
        await self.close()

    @property
    def queries_dict(self):
        return self._queries_dict
