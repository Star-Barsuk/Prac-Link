from .msg_base import MsgBase

class MsgConfigParser(MsgBase):
    class Success:
        @classmethod
        def table_load_queries(cls, table_name: str) -> None:
            MsgConfigParser._logger.debug(f"Queries for table '{table_name}' loaded successfully")

    class Failure:
        @classmethod
        def try_action_config(cls, table_config: str, exception: str) -> None:
            MsgConfigParser._logger.error(f"Exception while parsing '{table_config}' : {exception}")

        @classmethod
        def miss_database_key(cls, key: str) -> None:
            MsgConfigParser._logger.error(f"Missing '{key}' key in database config")

        @classmethod
        def miss_tables_key(cls, key: str) -> None:
            MsgConfigParser._logger.error(f"Missing '{key}' key in tables config")

        @classmethod
        def miss_database_keys(cls, keys: list[str]) -> None:
            formatted = ", ".join(f"'{k}'" for k in keys)
            MsgConfigParser._logger.error(f"Missing required database config keys: {formatted}")

        @classmethod
        def miss_query_keys(cls, table: str, keys: list[str]) -> None:
            formatted = ", ".join(f"'{k}'" for k in keys)
            MsgConfigParser._logger.error(f"Missing required query keys for table '{table}': {formatted}")

        @classmethod
        def miss_optional_query_keys(cls, table: str, keys: list[str]) -> None:
            formatted = ", ".join(f"'{k}'" for k in keys)
            MsgConfigParser._logger.warning(f"Missing optional query keys for table '{table}': {formatted}")

        @classmethod
        def empty_tables_config(cls) -> None:
            MsgConfigParser._logger.error("Tables config is empty or not a dictionary")

        @classmethod
        def invalid_queries_format(cls, table: str) -> None:
            MsgConfigParser._logger.error(f"Queries for table '{table}' must be a dictionary")

        @classmethod
        def miss_queries_for_tables(cls, tables: list[str]) -> None:
            formatted = ", ".join(f"'{t}'" for t in tables)
            MsgConfigParser._logger.warning(f"No query config found for tables: {formatted}")
