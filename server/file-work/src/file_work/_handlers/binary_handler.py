from typing import Optional
from .base_handler import BaseFileHandler

class BinaryHandler(BaseFileHandler):
    EXTENSIONS = ['bin']

    def write(self, file_path: str, data: str, overwrite: bool = False) -> bool:
        mode = 'wb' if overwrite else 'xb'
        with open(file_path, mode) as f:
            f.write(data.encode('utf-8'))
        return True

    def read(self,file_path: str) -> Optional[str]:
        with open(file_path, 'rb') as f:
            return f.read().decode('utf-8')
