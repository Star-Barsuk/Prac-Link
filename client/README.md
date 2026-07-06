<div align="center">

# Prac-Link Client

[![Android](https://img.shields.io/badge/Android-API%2029+-3DDC84?style=flat&logo=android&logoColor=white)](https://developer.android.com/) [![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/) [![OkHttp](https://img.shields.io/badge/OkHttp-4.x-3DDC84?style=flat)](https://square.github.io/okhttp/)

</div>

Android client for Prac-Link — a mobile app for students and practice supervisors.

---

## ⚙️ Features

- Login and registration with field validation
- Bottom navigation: practice, documents, chats, users, profile
- Practice placement selection by year, course, and group
- Document upload (multipart) and status display
- Chat list, messaging, and new dialog creation

---

## 📦 Quick Start

```bash
# 1. Go to the client directory
cd client

# 2. Configure the server URL
echo "server.url=http://10.0.2.2:8000" >> local.properties

# 3. Build a debug APK
./gradlew assembleDebug

# 4. Install on emulator or device
./gradlew installDebug
```

Open the project in Android Studio: **File → Open → `client/`**.

---

## 🗂️ Package Structure

```text
com.example.client/
├── LoginActivity.java          # Login screen (launcher)
├── RegisterActivity.java       # Registration
├── MainActivity.java           # Main screen with bottom navigation
├── ChatActivity.java           # Messaging screen
├── AuthBaseActivity.java       # Base class for auth screens
│
├── bottom_nav/                 # Bottom navigation fragments
│   ├── practice/PracticeFragment.java
│   ├── documents/DocumentsFragment.java
│   ├── chats/ChatsFragment.java
│   ├── users/UsersFragment.java
│   ├── profile/ProfileFragment.java
│   ├── BaseFragment.java
│   └── BaseDialog.java
│
├── network/                    # HTTP layer (OkHttp)
│   ├── ApiRequest.java         # Base request class
│   ├── ServerConfig.java       # BASE_URL from BuildConfig
│   ├── AuthRequests.java
│   ├── UserRequests.java
│   ├── ChatRequests.java
│   ├── MessageRequests.java
│   ├── PracticeRequests.java
│   └── DocumentRequests.java
│
├── models/                     # POJO models
│   ├── User.java
│   ├── Chat.java
│   ├── Message.java
│   ├── PracticeBase.java
│   └── ...
│
├── adapters/                   # RecyclerView adapters
│   ├── ChatAdapter.java
│   ├── MessageAdapter.java
│   ├── UserAdapter.java
│   └── ...
│
├── view_models/
│   └── UserViewModel.java      # Current user singleton
│
└── utils/
    └── HashPassword.java       # Password hashing (SHA-256)
```

---

## ⚙️ Architecture

### Navigation

`MainActivity` manages five fragments via `BottomNavigationView`. The back button returns to the Practice tab; pressing again exits the app.

### Network Layer

All requests go through `ApiRequest`:

- JSON requests: `GET`, `POST`, `PUT`, `DELETE`
- File uploads: `sendMultipartRequest` in `DocumentRequests`
- Server URL: `ServerConfig.BASE_URL` → `BuildConfig.SERVER_URL`

### User State

`UserViewModel` is a singleton with `LiveData<User>`. When `user == null`, `MainActivity` redirects to `LoginActivity`.

---

## 🔗 Server Configuration

The URL is set in `client/local.properties`:

```properties
server.url=http://10.0.2.2:8000
```

| Environment | `server.url` value |
|-------------|-------------------|
| Emulator | `http://10.0.2.2:8000` |
| Physical device | `http://<host-IP>:8000` |

The value is injected into `BuildConfig.SERVER_URL` via `app/build.gradle.kts`.

---

## 🚀 Build

| Parameter | Value |
|-----------|-------|
| `applicationId` | `com.example.client` |
| `minSdk` | 29 |
| `targetSdk` / `compileSdk` | 35 |
| Java | 17 |
| ViewBinding | enabled |
| Cleartext HTTP | allowed (`usesCleartextTraffic`) |

```bash
./gradlew assembleDebug     # debug APK
./gradlew assembleRelease   # release APK (requires keystore.properties)
./gradlew test              # unit tests
```

---

## 📋 Dependencies

| Library | Purpose |
|---------|---------|
| OkHttp | HTTP client |
| org.json | JSON parsing |
| Material Components | UI components |
| AndroidX Navigation | Navigation |
| AndroidX AppCompat | Base activities |

Versions are defined in `client/gradle/libs.versions.toml`.

---

## 📱 Activities

| Activity | Role |
|----------|------|
| `LoginActivity` | Entry point, authentication |
| `RegisterActivity` | Account creation |
| `MainActivity` | Main tabbed UI |
| `ChatActivity` | View and send messages |

---

<div align="center">

**© 2026 Star-Barsuk**

</div>
