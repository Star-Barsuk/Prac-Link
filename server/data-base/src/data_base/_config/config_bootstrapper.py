from typing import Optional
from .load.database_config_loader import DataBaseConfigLoader
from .load.tables_config_loader import TablesConfigLoader
from .load.queries_config_loader import QueriesConfigLoader
from .parse.config_parser import ConfigParser

class ConfigBootstrapper:
    def __init__(self, config_path: str):
        self.config_path = config_path
        self.database_config = None
        self.tables_config = None
        self.queries_dict = {}

    def bootstrap(self) -> Optional[bool]:
        database_loader = DataBaseConfigLoader(self.config_path)
        if not database_loader.load_config():
            return None
        self.database_config = database_loader.database_config

        tables_loader = TablesConfigLoader(self.config_path)
        if not tables_loader.load_config():
            return None
        self.tables_config = tables_loader.tables_config

        if not ConfigParser.validate_tables(self.tables_config):
            return None

        queries_loader = QueriesConfigLoader(self.config_path)
        for table_name in self.tables_config.get("tables", {}):
            queries = queries_loader.load_config(table_name)
            if queries:
                self.queries_dict[table_name] = queries

        if not self.queries_dict:
            return None

        if not ConfigParser.validate(
                database_config=database_loader.database_config,
                tables_config=tables_loader.tables_config,
                queries_dict=self.queries_dict
        ):
            return None
        return True

    def get_database_config(self):
        return self.database_config["database"]

    def get_tables_config(self):
        return self.tables_config

    def get_queries_dict(self):
        return self.queries_dict