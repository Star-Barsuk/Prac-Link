from typing import Type
from .._handlers.json_handler import JsonHandler
from .._handlers.binary_handler import BinaryHandler
from .._handlers.base_handler import BaseFileHandler

class FileHandlerFactory:
    ALL_HANDLERS: list[Type[BaseFileHandler]] = [JsonHandler, BinaryHandler]

    @classmethod
    def create_handler(cls, extension: str) -> BaseFileHandler:
        ext = extension.lower()
        for handler_cls in cls.ALL_HANDLERS:
            if ext in handler_cls.EXTENSIONS:
                return handler_cls()
        return BaseFileHandler()