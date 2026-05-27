from fastapi import FastAPI
from data_base import Database, MyDatabaseError

async def on_startup():
    try:
        await Database.init()
    except MyDatabaseError as e:
        print(f"Failed to initialize database: {e}")
        raise

async def on_shutdown():
    await Database.close_instance()

def register_events(app: FastAPI):
    app.add_event_handler("startup", on_startup)
    app.add_event_handler("shutdown", on_shutdown)
