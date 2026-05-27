from abc import ABC, abstractmethod
from typing import Any, Optional

class BaseFileHandler(ABC):
    EXTENSIONS = []

    @abstractmethod
    def write(self, file_path: str, data: Any, overwrite: bool = False) -> bool:
        pass

    @abstractmethod
    def read(self, file_path: str) -> Optional[Any]:
        pass
