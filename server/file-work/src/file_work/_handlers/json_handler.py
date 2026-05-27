import json
from typing import Optional
from .base_handler import BaseFileHandler

class JsonHandler(BaseFileHandler):
    EXTENSIONS = ['json']

    def write(self,file_path: str, data: dict, overwrite: bool = False) -> bool:
        mode = 'w' if overwrite else 'x'
        with open(file_path, mode, encoding='utf-8') as f:
            json.dump(data, f)
        return True

    def read(self,file_path: str) -> Optional[dict]:
        with open(file_path, 'r', encoding='utf-8') as f:
            return json.load(f)
