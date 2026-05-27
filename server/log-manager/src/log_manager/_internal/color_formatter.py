import logging
from typing import Optional, Dict, Literal
from colorama import Fore, Style


class ColorFormatter(logging.Formatter):
    COLOR_MAP: Dict[str, str] = {
        'DEBUG': Fore.CYAN,
        'INFO': Fore.GREEN,
        'WARNING': Fore.YELLOW,
        'ERROR': Fore.RED,
        'CRITICAL': Fore.MAGENTA + Style.BRIGHT,
    }

    def __init__(self, fmt: Optional[str] = None, date_fmt: Optional[str] = None, style: Literal["%", "{", "$"] = "%"):
        super().__init__(fmt=fmt, datefmt=date_fmt, style=style)

    def format(self, record: logging.LogRecord) -> str:
        color = self.COLOR_MAP.get(record.levelname, "")
        message = super().format(record)
        return f"{color}{message}{Style.RESET_ALL}"
