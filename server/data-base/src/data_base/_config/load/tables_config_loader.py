from ..._shared.messages.msg_config_load import MsgConfigLoad
from file_work import File

class TablesConfigLoader:
    def __init__(self, config_folder: str):
        self.config_folder = config_folder
        self.tables_config = None

    def load_config(self) -> bool:
        config_path = f"{self.config_folder}/tables_config.json"
        try:
            self.tables_config = File.read(file_path=config_path)
            if not self.tables_config:
                raise ValueError("Config is empty or None")
            MsgConfigLoad.Success.tables_load_config()
            return True
        except Exception as e:
            MsgConfigLoad.Failure.tables_load_config(str(e))
            return False
