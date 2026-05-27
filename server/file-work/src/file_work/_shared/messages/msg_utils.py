from .msg_base import MsgBase

class MsgUtils(MsgBase):
    class Failure:
        @classmethod
        def invalid_extension(cls, extension: str, valid_extensions: list[str]) -> None:
            MsgUtils._logger.error(
                f"Invalid extension '{extension}', allowed: {', '.join(valid_extensions)}"
            )

        @classmethod
        def file_not_found(cls, file_path: str) -> None:
            MsgUtils._logger.error(f"Not found file './{file_path}'")

        @classmethod
        def file_exists(cls, file_path: str) -> None:
            MsgUtils._logger.warning(f"Already exist file './{file_path}'")
