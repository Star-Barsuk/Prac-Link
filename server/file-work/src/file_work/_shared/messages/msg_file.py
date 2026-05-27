from .msg_base import MsgBase

class MsgFile(MsgBase):
    class Success:
        @classmethod
        def file_read(cls, file_path: str) -> None:
            MsgFile._logger.debug(f"Read successful file './{file_path}'")

        @classmethod
        def file_write(cls, file_path: str, overwrite: bool = False) -> None:
            if overwrite:
                MsgFile._logger.debug(f"Write successful file './{file_path}'")
            else:
                MsgFile._logger.debug(f"Write successful with mode [overwrite] file './{file_path}'")

    class Failure:
        @classmethod
        def file_read(cls, file_path: str, error: str) -> None:
            MsgFile._logger.error(f"Read failed file './{file_path}': {error}")

        @classmethod
        def file_write(cls, file_path: str, error: str) -> None:
            MsgFile._logger.error(f"Write failed file './{file_path}': {error}")
