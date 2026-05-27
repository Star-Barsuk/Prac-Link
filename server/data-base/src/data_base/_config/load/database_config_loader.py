from ..._shared.messages.msg_config_load import MsgConfigLoad
from file_work import File

class DataBaseConfigLoader:
    def __init__(self, config_folder: str):
        self.config_folder = config_folder
        self.database_config = None

    def load_config(self) -> bool:
        config_path = f"{self.config_folder}/database_config.json"
        try:
            self.database_config = File.read(file_path=config_path)
            if not self.database_config:
                raise ValueError("Config is empty or None")
            MsgConfigLoad.Success.database_load_config()
            return True
        except Exception as e:
            MsgConfigLoad.Failure.database_load_config(str(e))
            return False
