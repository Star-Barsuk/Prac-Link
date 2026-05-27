import os
from typing import List, Optional

from  .._shared.messages.msg_utils import MsgUtils

def build_path(folder: str, filename: str, extension: str) -> str:
    return os.path.join(folder, f"{filename}.{extension}")

def resolve_file_path(*,
                      file_path: Optional[str] = None,
                      folder: Optional[str] = None,
                      filename: Optional[str] = None,
                      extension: Optional[str] = None,
                      valid_extensions: Optional[List[str]] = None,
                      mode: str = "read",
                      overwrite: bool = False) -> Optional[str]:
    if file_path:
        ext = extension or os.path.splitext(file_path)[1][1:]
    else:
        if None in (folder, filename, extension):
            MsgUtils.Failure.invalid_extension(extension or '', valid_extensions or [])
            return None
        file_path = build_path(folder, filename, extension)
        ext = extension

    ext = ext.lower()
    if valid_extensions and ext not in valid_extensions:
        MsgUtils.Failure.invalid_extension(ext, valid_extensions)
        return None

    file_exists = os.path.isfile(file_path)

    if mode == "read" and not file_exists:
        MsgUtils.Failure.file_not_found(file_path)
        return None
    if mode == "write" and file_exists and not overwrite:
        MsgUtils.Failure.file_exists(file_path)
        return None

    return file_path
