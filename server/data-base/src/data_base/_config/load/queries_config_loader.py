from ..._shared.messages.msg_config_load import MsgConfigLoad
from file_work import File

class QueriesConfigLoader:
    def __init__(self, config_folder: str):
        self.config_folder = config_folder

    def load_config(self, table_name):
        config_path = f"{self.config_folder}/queries/{table_name}.json"
        try:
            config = File.read(file_path=config_path)
            if not config:
                MsgConfigLoad.Failure.table_load_queries(table_name, "Config is empty or None")
            MsgConfigLoad.Success.table_load_queries(table_name)
            return config
        except Exception as e:
            MsgConfigLoad.Failure.try_action_config(table_name, str(e))
            return None
