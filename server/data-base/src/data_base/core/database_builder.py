from typing import Optional
from .._config.config_bootstrapper import ConfigBootstrapper
from .._infrastructure.aiomysql.aiomysql_connection import AioMySQLConnection
from .._infrastructure.database_wrapper import DatabaseWrapper
from .._shared.messages.msg_database import MsgDataBase

class DatabaseBuilder:
    def __init__(self, config_path: str)-> None:
        self._config_path = config_path

    async def build(self) -> Optional[DatabaseWrapper]:
        bootstrapper = ConfigBootstrapper(self._config_path)
        if not bootstrapper.bootstrap():
            MsgDataBase.Failure.config_bootstrap_failed()
            return None

        connection = AioMySQLConnection(bootstrapper.get_database_config())
        if not await connection.connect():
            return None

        return DatabaseWrapper(
            connection=connection,
            queries_dict=bootstrapper.get_queries_dict()
        )
