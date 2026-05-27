from typing import Dict, Any, Optional
from ..._shared.messages.msg_config_parser import MsgConfigParser

class ConfigParser:
    @staticmethod
    def validate(
            database_config: Dict[str, Any],
            tables_config: Dict[str, Any],
            queries_dict: Dict[str, Any]
    ) -> bool:
        return (
                ConfigParser._validate_database(database_config)
                and ConfigParser._validate_queries(queries_dict, tables_config)
        )

    @staticmethod
    def _validate_database(config: Dict[str, Any]) -> bool:
        database_key = "database"
        required_fields = {"host", "user", "password", "name"}
        try:
            if database_key not in config:
                MsgConfigParser.Failure.miss_database_key(database_key)
                return False

            database_config = config[database_key]
            missing_fields = list(required_fields - set(database_config.keys()))

            if missing_fields:
                MsgConfigParser.Failure.miss_database_keys(missing_fields)
                return False
        except Exception as e:
            MsgConfigParser.Failure.try_action_config(database_key, str(e))
            return False
        return True

    @staticmethod
    def validate_tables(config: Dict[str, Any]) -> bool:
        tables_key = "tables"
        try:
            if tables_key not in config:
                MsgConfigParser.Failure.miss_tables_key(tables_key)
                return False

            tables = config[tables_key]
            if not isinstance(tables, dict) or not tables:
                MsgConfigParser.Failure.empty_tables_config()
                return False
        except Exception as e:
            MsgConfigParser.Failure.try_action_config(tables_key, str(e))
            return False
        return True

    @staticmethod
    def _validate_queries(queries_dict: Dict[str, Any], tables_config: Dict[str, Any]) -> bool:
        expected_tables = set(tables_config.get("tables", {}).keys())
        try:
            actual_tables = set(queries_dict.keys())
            missing_queries = list(expected_tables - actual_tables)

            if missing_queries:
                MsgConfigParser.Failure.miss_queries_for_tables(missing_queries)

            for table_name, queries in queries_dict.items():
                if not isinstance(queries, dict):
                    MsgConfigParser.Failure.invalid_queries_format(table_name)
                    return False

                required_keys = {"create", "drop"}
                optional_keys = {"insert", "delete", "update", "fetch"}

                present_keys = set(queries.keys())
                missing_required = list(required_keys - present_keys)
                missing_optional = list(optional_keys - present_keys)

                if missing_required:
                    MsgConfigParser.Failure.miss_query_keys(table_name, missing_required)
                    return False

                if missing_optional:
                    MsgConfigParser.Failure.miss_optional_query_keys(table_name, missing_optional)
        except Exception as e:
            MsgConfigParser.Failure.try_action_config("queries_config", str(e))
            return False
        return True
