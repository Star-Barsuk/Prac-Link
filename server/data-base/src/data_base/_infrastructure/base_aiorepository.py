import aiomysql
from typing import Tuple, Optional, List, Union
from .aiomysql.aiomysql_repository import AioMySQLRepository

class BaseAioRepository(AioMySQLRepository):
    def __init__(self, pool: aiomysql.Pool, queries: dict, table_name: Optional[str] = None) -> None:
        super().__init__(pool, queries)
        self._table_name = table_name

    def _resolve_query(self, category: str, query_key: str) -> str:
        return self._queries[category][query_key]

    async def fetch_one(self, query_key: str, params: Tuple = ()) -> Optional[dict]:
        query = self._resolve_query("fetch", query_key)
        result = await super().fetch(query, params, fetch_mode="one")
        return result if isinstance(result, dict) else None

    async def fetch_many(self, query_key: str, params: Tuple = ()) -> Union[List[dict], None]:
        query = self._resolve_query("fetch", query_key)
        result = await super().fetch(query, params, fetch_mode="many")
        return  result if isinstance(result, list) else None

    async def create(self) -> int:
        return await super().execute(self._queries["create"])

    async def drop(self) -> int:
        return await super().execute(self._queries["drop"])

    async def insert(self, data: Tuple) -> int:
        return await super()._insert(self._queries["insert"], data)

    async def update(self, data: Tuple, condition: Tuple) -> int:
        return await super().execute(self._queries["update"], (*data, *condition))

    async def delete(self, name: str, condition: Tuple) -> int:
        query = self._resolve_query("delete", name)
        return await super().execute(query, condition)
