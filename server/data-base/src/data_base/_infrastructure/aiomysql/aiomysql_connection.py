from typing import Optional
import aiomysql
from ..._shared.messages.msg_database import MsgDataBase

class AioMySQLConnection:
    def __init__(self, config: dict) -> None:
        self._config = config
        self._pool: Optional[aiomysql.Pool] = None

    async def connect(self) -> bool:
        try:
            port = self._config.get("port", 3306)
            self._pool = await aiomysql.create_pool(
                host=self._config["host"],
                user=self._config["user"],
                password=self._config["password"],
                db=self._config["name"],
                port=port,
                autocommit=True
            )
            MsgDataBase.Success.connection_established()
            return True
        except Exception as e:
            MsgDataBase.Failure.connection_failed(str(e))
            return False

    @property
    def is_connected(self) -> bool:
        return self._pool is not None and not self._pool.closed

    async def close(self):
        if self._pool:
            self._pool.close()
            await self._pool.wait_closed()

    @property
    def pool(self):
        return self._pool
