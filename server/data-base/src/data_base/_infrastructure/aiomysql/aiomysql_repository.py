from typing import Any, Tuple, Optional, List
import aiomysql
from ..._shared.messages.msg_database import MsgDataBase

class AioMySQLRepository:
    def __init__(self, pool: aiomysql.Pool, queries: dict) -> None:
        self._pool = pool
        self._queries = queries

    async def execute(self, query: str, params: Tuple = ()) -> int:
        try:
            async with self._pool.acquire() as conn:
                async with conn.cursor() as cur:
                    await cur.execute(query, params)
                    return cur.rowcount
        except Exception as e:
            MsgDB.Failure.query_failed(query, str(e))
            return -1

    async def _insert(self, query: str, params: Tuple = ()) -> int:
        try:
            async with self._pool.acquire() as conn:
                async with conn.cursor() as cur:
                    await cur.execute(query, params)
                    await conn.commit()
                    return cur.lastrowid
        except Exception as e:
            MsgDB.Failure.query_failed(query, str(e))
            return -1

    async def fetch(self, query: str, params: Tuple = (), fetch_mode="one") -> Any:
        try:
            async with self._pool.acquire() as conn:
                async with conn.cursor(aiomysql.DictCursor) as cur:
                    await cur.execute(query, params)
                    if fetch_mode == "one":
                        return await cur.fetchone()
                    return await cur.fetchall()
        except Exception as e:
            MsgDB.Failure.query_failed(query, str(e))
            return None
