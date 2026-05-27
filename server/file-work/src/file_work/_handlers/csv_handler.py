import pandas as pd
from typing import Optional
from .base_handler import BaseFileHandler

class CsvHandler(BaseFileHandler):
    def write(self,file_path: str, data: pd.DataFrame, overwrite: bool = False) -> bool:
        mode = 'w' if overwrite else 'x'
        data.to_csv(file_path, index=False, mode=mode)
        return True

    def read(self,file_path: str) -> Optional[pd.DataFrame]:
        return pd.read_csv(file_path)
