import os
from typing import Any, Optional

from .._internal.handler_factory import FileHandlerFactory as Factory
from .._utils.path_utils import resolve_file_path
from .._shared.messages.msg_file import MsgFile


class File:
    SUPPORTED_EXTENSIONS = ['json', 'bin']

    @classmethod
    def read(cls, *,
             file_path: Optional[str] = None,
             folder: Optional[str] = None,
             filename: Optional[str] = None,
             extension: Optional[str] = None) -> Optional[Any]:
        full_path = resolve_file_path(
            file_path=file_path,
            folder=folder,
            filename=filename,
            extension=extension,
            valid_extensions=cls.SUPPORTED_EXTENSIONS,
            mode="read"
        )

        if not full_path:
            return None

        try:
            ext = extension or full_path.split('.')[-1]
            handler = Factory.create_handler(ext)
            data = handler.read(full_path)
            MsgFile.Success.file_read(full_path)
            return data
        except Exception as e:
            MsgFile.Failure.file_read(full_path, str(e))
            return None

    @classmethod
    def write(cls, data: Any, *, file_path: Optional[str] = None,
              folder: Optional[str] = None,
              filename: Optional[str] = None,
              extension: Optional[str] = None,
              overwrite: bool = False) -> bool:
        full_path = resolve_file_path(
            file_path=file_path,
            folder=folder,
            filename=filename,
            extension=extension,
            valid_extensions=cls.SUPPORTED_EXTENSIONS,
            mode="write",
            overwrite=overwrite
        )

        if not full_path:
            return False

        try:
            os.makedirs(os.path.dirname(full_path), exist_ok=True)
            ext = extension or full_path.split('.')[-1]
            handler = Factory.create_handler(ext)
            handler.write(full_path, data, overwrite)
            MsgFile.Success.file_write(full_path, overwrite)
            return True
        except Exception as e:
            MsgFile.Failure.file_write(full_path, str(e))
            return False
