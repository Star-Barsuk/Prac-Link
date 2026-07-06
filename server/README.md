<div align="center">

# Prac-Link Server

[![FastAPI](https://img.shields.io/badge/FastAPI-0.115-009688?style=flat&logo=fastapi&logoColor=white)](https://fastapi.tiangolo.com/) [![Python](https://img.shields.io/badge/Python-3.13+-3776AB?style=flat&logo=python&logoColor=white)](https://www.python.org/) [![MySQL](https://img.shields.io/badge/MySQL-8.4-4479A1?style=flat&logo=mysql&logoColor=white)](https://www.mysql.com/) [![Poetry](https://img.shields.io/badge/Poetry-2.x-60A5FA?style=flat&logo=poetry&logoColor=white)](https://python-poetry.org/)

</div>

FastAPI REST API for the Prac-Link mobile client: users, study groups, practice placements, documents, and chats.

---

## ⚙️ Features

- Async MySQL access via the `data-base` module
- CRUD for users, roles, courses, groups, and practice placements
- Practice enrollment and document management
- Chats, members, and messages (REST)
- Database schema reset and recreation (`POST /admin/reset`)
- Centralized logging (`log-manager`)

---

## 📦 Quick Start

```bash
# 1. Go to the server directory
cd server

# 2. Start MySQL and phpMyAdmin
docker compose up -d

# 3. Install dependencies
poetry install

# 4. Run the server
poetry run python main.py
```

The server starts at `http://0.0.0.0:8000` with hot-reload.

**Seed test data:**

```bash
bash scripts/insert_test_data_1.sh   # full dataset
bash scripts/insert_test_data_2.sh   # alternative dataset
```

---

## 🗂️ Project Structure

```text
server/
├── main.py                  # FastAPI entry point + CORS
├── app/
│   ├── api/
│   │   ├── __init__.py      # Router assembly
│   │   └── routes/          # Endpoints by domain
│   ├── schemas/
│   │   ├── input/           # Pydantic request models
│   │   └── output/          # Pydantic response models
│   └── events.py            # Startup / shutdown (DB init)
├── config/
│   ├── database/
│   │   ├── database_config.json
│   │   ├── tables_config.json
│   │   └── queries/         # SQL queries per table
│   └── logging_config.json
├── data-base/               # data_base package (aiomysql, repositories)
├── file-work/               # file_work package (file handling)
├── log-manager/             # log_manager package
├── scripts/                 # Bash scripts for test data
├── docker-compose.yml
└── pyproject.toml
```

---

## 🔌 API Routes

| Prefix | Tag | Purpose |
|--------|-----|---------|
| `/admin` | Admin | Database reset |
| `/users` | Users | Registration, login, profile |
| `/roles` | Roles | User roles |
| `/years` | Years | Academic years |
| `/courses` | Courses | Courses |
| `/groups` | Groups | Groups |
| `/student-groups` | Student Groups | Student-to-group assignments |
| `/practice` | Practice | Practice placements and enrollments |
| `/chats` | Chats | Chats |
| `/chat-members` | Chat Members | Chat participants |
| `/messages` | Messages | Messages |
| `/documents` | Documents | File upload and status |

Interactive docs: `http://localhost:8000/docs`.

---

## ⚙️ Configuration

### Database

`config/database/database_config.json`:

```json
{
  "database": {
    "host": "localhost",
    "user": "prac-4-admin",
    "password": "admin",
    "name": "prac-4",
    "port": 3307
  }
}
```

Settings match `docker-compose.yml` (port **3307** on the host).

### Docker Services

| Service | Port | Description |
|---------|------|-------------|
| MySQL 8.4 | `3307` | Database `prac-4` |
| phpMyAdmin | `8080` | Web DB interface |

---

## 🚀 Commands

| Command | Action |
|---------|--------|
| `poetry install` | Install dependencies |
| `poetry run python main.py` | Run with reload |
| `docker compose up -d` | Start MySQL + phpMyAdmin |
| `docker compose down` | Stop containers |
| `bash scripts/insert_test_data_1.sh` | Seed database with test data |

---

## 📋 Requirements

| Component | Version |
|-----------|---------|
| Python | 3.13+ |
| Poetry | 2.x |
| Docker / Docker Compose | Latest |
| curl, jq | For scripts in `scripts/` |

---

## 📦 Local Packages

| Package | Path | Purpose |
|---------|------|---------|
| `data-base` | `data-base/` | Async MySQL access, repositories |
| `log-manager` | `log-manager/` | JSON-based logging setup |
| `file-work` | `file-work/` | CSV, JSON, and binary file I/O |

More on the database module: [data-base/README.md](data-base/README.md).

---

## 🔧 Application Lifecycle

1. **Startup** — `Database.init()` connects to MySQL and prepares repositories
2. **Request** — routes receive repositories via `Depends`
3. **Shutdown** — `Database.close_instance()` closes connections

---

<div align="center">

**© 2026 Star-Barsuk**

</div>
