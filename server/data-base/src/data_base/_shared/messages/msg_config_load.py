from .msg_base import MsgBase

class MsgConfigLoad(MsgBase):
    class Success:
        @classmethod
        def database_load_config(cls) -> None:
            MsgConfigLoad._logger.debug(f"Database config loaded successfully")

        @classmethod
        def tables_load_config(cls) -> None:
            MsgConfigLoad._logger.debug(f"Tables config loaded successfully")

        @classmethod
        def table_load_queries(cls, table_name: str) -> None:
            MsgConfigLoad._logger.debug(f"Queries for table '{table_name}' loaded successfully")

    class Failure:
        @classmethod
        def try_action_config(cls, table_name: str, exception: str) -> None:
            MsgConfigLoad._logger.error(f"Exception while loading config for '{table_name}' : {exception}")

        @classmethod
        def database_load_config(cls, error: str) -> None:
            MsgConfigLoad._logger.error(f"The Database Config was not uploaded: {error}")

        @classmethod
        def tables_load_config(cls, error: str) -> None:
            MsgConfigLoad._logger.error(f"The Tables Config was not uploaded: {error}")

        @classmethod
        def table_load_queries(cls, table_name: str, error: str) -> None:
            MsgConfigLoad._logger.warning(f"Queries for table '{table_name}' was not uploaded: {error}")
