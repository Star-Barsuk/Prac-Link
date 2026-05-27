from .msg_base import MsgBase

class MsgDataBase(MsgBase):
    class Success:
        @classmethod
        def connection_established(cls) -> None:
            MsgDataBase._logger.debug("Database connection established successfully")

    class Failure:
        @classmethod
        def connection_failed(cls, error: str) -> None:
            MsgDataBase._logger.error(f"Database connection failed: {error}")

        @classmethod
        def query_failed(cls, query: str, error: str) -> None:
            MsgDataBase._logger.error(f"Query failed: {query} | Error: {error}")

        @classmethod
        def query_miss_failed(cls, table_name: str) -> None:
            MsgDataBase._logger.warning(f"Miss query for table: {table_name}")

        @staticmethod
        def instance_build_failed() -> None:
            MsgDataBase._logger.critical("Database instance build failed")

        @staticmethod
        def config_bootstrap_failed() -> None:
            MsgDataBase._logger.critical("Failed to bootstrap the database configuration")

        @staticmethod
        def instance_not_build() -> None:
            MsgDataBase._logger.critical("Database instance is not built")
