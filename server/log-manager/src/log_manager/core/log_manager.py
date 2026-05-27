import logging
import logging.config
import json
from pathlib import Path
from typing import Optional, Dict, Any
from colorama import init

from .._internal.color_formatter import ColorFormatter

init(autoreset=True)


class LogManager:
    _instance = None

    DEFAULT_FORMAT = "%(asctime)s [%(levelname)s] [%(name)s] - %(message)s"
    DEFAULT_DATE_FORMAT = "%Y-%m-%d %H:%M:%S"
    DEFAULT_LEVEL = logging.INFO
    DEFAULT_CONFIG_PATH = "config/logging_config.json"

    def __new__(cls, *args, **kwargs):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self, config_path: Optional[str] = None):
        if not hasattr(self, '_initialized'):
            self._config_path = config_path or self.DEFAULT_CONFIG_PATH
            self._logger = logging.getLogger(self.__class__.__name__)
            self._initialized = True

    def setup_logging(self) -> bool:
        config = self._load_config()
        if config is None:
            self._setup_default_logging()
            return False

        try:
            self._ensure_logs_dir(config)
            logging.config.dictConfig(config)
            self._apply_color_formatters(config)
            return True
        except Exception as e:
            self._logger.error(f"Failed to setup logging: {str(e)}", exc_info=True)
            self._setup_default_logging()
            return False

    def _setup_default_logging(self) -> None:
        handler = logging.StreamHandler()
        handler.setFormatter(ColorFormatter(
            fmt=self.DEFAULT_FORMAT,
            date_fmt=self.DEFAULT_DATE_FORMAT
        ))

        logging.basicConfig(
            level=self.DEFAULT_LEVEL,
            handlers=[handler]
        )

    def _load_config(self) -> Optional[Dict[str, Any]]:
        try:
            config_file = Path(self._config_path)
            if not config_file.exists():
                self._logger.error(f"Logging config file not found at: {self._config_path}")
                return None

            with config_file.open('r', encoding='utf-8') as f:
                return json.load(f)
        except json.JSONDecodeError:
            self._logger.error(f"Invalid JSON format in logging config file: {self._config_path}")
        except Exception as e:
            self._logger.error(f"Error loading config: {str(e)}")
        return None

    def _ensure_logs_dir(self, config: Dict[str, Any]) -> None:
        has_file_handler = any(
            handler.get("class", "").endswith("FileHandler")
            for handler in config.get("handlers", {}).values()
        )
        if has_file_handler:
            Path("logs").mkdir(exist_ok=True)

    def _apply_color_formatters(self, config: Dict[str, Any]) -> None:
        fmt = self.DEFAULT_FORMAT
        date_fmt = self.DEFAULT_DATE_FORMAT

        if config:
            formatters = config.get("formatters", {})
            default_formatter = formatters.get("default", {})
            fmt = default_formatter.get("format", fmt)
            date_fmt = default_formatter.get("datefmt", date_fmt)

        root = logging.getLogger()
        for handler in root.handlers:
            if isinstance(handler, logging.StreamHandler):
                handler.setFormatter(ColorFormatter(fmt=fmt, date_fmt=date_fmt))

        for name, logger in logging.Logger.manager.loggerDict.items():
            if isinstance(logger, logging.Logger):
                for handler in logger.handlers:
                    if isinstance(handler, logging.StreamHandler):
                        handler.setFormatter(ColorFormatter(fmt=fmt, date_fmt=date_fmt))
