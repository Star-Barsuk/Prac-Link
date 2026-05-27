# Database Module
## Features
- Asynchronous MySQL support via `aiomysql`
- Centralized repository management with `DatabaseWrapper`
- Safe and reusable singleton database instance
- Detailed error logging `(no async)`
---
## Configuration Files
### 1. `config/database_config.json`
```json
{
  "host": "localhost",
  "port": "3306",
  "user": "root",
  "password": "password",
  "name": "your_database"
}
```
### 2. `config/tables_config.json`
```json
{
  "tables" : {
    "users": {}
  }
}
```
### 3. `config/queries/users.json`
```json
{
    "create": "CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, username VARCHAR(255) NOT NULL UNIQUE, email VARCHAR(255) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL, created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);",
    "drop": "DROP TABLE IF EXISTS users;",
    "insert": "INSERT INTO users (username, email, password_hash) VALUES (%s, %s, %s);",
    "delete": {
        "by_id": "DELETE FROM users WHERE id = %s;"
    },
    "update": "UPDATE users SET username = %s, email = %s, password_hash = %s WHERE id = %s;",
    "fetch": {
        "by_id": "SELECT * FROM users WHERE id = %s;",
        "by_username": "SELECT * FROM users WHERE username = %s;",
        "by_email": "SELECT * FROM users WHERE email = %s;",
        "all": "SELECT * FROM users;",
        "all_except": "SELECT * FROM users WHERE id != %s;",
        "count": "SELECT COUNT(*) FROM users;"
    }
}
```
### 4. `config/logging_config.json`
```json
{
  "version": 1,
  "formatters": {
    "default": {
      "format": "%(asctime)s [%(levelname)s] [%(name)s] - %(message)s."
    }
  },
  "handlers": {
    "console": {
      "class": "logging.StreamHandler",
      "level": "DEBUG",
      "formatter": "default",
      "stream": "ext://sys.stdout"
    }
  },
  "loggers": {
    "file-work": {
      "level": "ERROR",
      "handlers": ["console"],
      "propagate": false
    },
     "data-base": {
      "level": "DEBUG",
      "handlers": ["console"],
      "propagate": false
    }
  }
}
```
---
## Usage
### 1. Logger
```python
from log_manager import LogManager

log_manager = LogManager("config/logging_config.json")
log_manager.setup_logging()
```
### 2. Database
```python
import asyncio
from data_base import Database, DatabaseError

async def run():
    try:
        async with Database() as db:
            await Database.reset()  # Drop + Create all tables

            users_repo = db.get_repository("users")
            await users_repo.create()

            await users_repo.insert(("alice", "alice@example.com", "hashed_pw"))
            await users_repo.insert(("bob", "bob@example.com", "hashed_pw2"))

            user = await users_repo.fetch_one("by_username", ("alice",))
            print("Fetched user:", user)

            user_id = user["id"] if user else None
            user_by_id = await users_repo.fetch_one("by_id", (user_id,))
            print("Fetched by ID:", user_by_id)

            all_users = await users_repo.fetch_many("all")
            print("All users:", all_users)

            await users_repo.update(("alice_updated", "alice_new@example.com", "new_pw"), (user_id,))
            updated_user = await users_repo.fetch_one("by_id", (user_id,))
            print("Updated user:", updated_user)

            await users_repo.delete("by_id", (user_id,))
            after_delete = await users_repo.fetch_many("all")
            print("After delete:", after_delete)

    except DatabaseError as e:
        print(f"Database error: {e}")
    finally:
        await Database.close_instance()

if __name__ == "__main__":
    asyncio.run(run())
```
---
## Notes
- Always use `Database.reset()` to recreate tables.
- Always close the Database using `Database.close_instance()`.
---