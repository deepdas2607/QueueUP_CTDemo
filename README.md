# QueueUp — Smart Digital Queue Application

QueueUp is a smart digital queue/waiting-list Android application designed for campus service counters (College Administration, Library Help Desk, Student Services, IT Support, Canteen Counter).

---

## Key Features

1. **User Authentication**: Secure registration and login with bcrypt password hashing, case-insensitive email verification, and JWT session tokens.
2. **Services Catalog**: Browse campus counters, real-time waiting counts, and estimated service times.
3. **Queue Token Management**: Join queue, view live position (#4), people ahead (3), and estimated wait time (18 min).
4. **Queue Lifecycle**: Refresh queue status, leave active queue token (`WAITING` -> `CANCELLED`).
5. **Queue History**: Track past completed and cancelled queue tickets.
6. **Profile & Settings**: View user details, queue statistics (Total Joined, Completed, Cancelled), notification toggle, and privacy consent toggle.
7. **System Notifications**: Local Android system notifications for turn alerts and queue position updates.
8. **Independent CleverTap Integration Module**: Standalone Phase 1-3 analytics, push, in-app, inbox, and server-side API integration package (decoupled from Core App).

---

## Database Choice: SQLite

We are using **SQLite** via Prisma ORM for the backend.
- **Why SQLite?**: SQLite is zero-configuration, file-based (`file:./dev.db`), and self-contained within the project folder. No external PostgreSQL database server needs to be installed, running, or managed. It fits the backend architecture perfectly.

---

## Architecture

### Android Application
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose with Material 3
- **Networking**: Retrofit 2 + OkHttp 4
- **Architecture Pattern**: MVVM (Compose Screen -> ViewModel -> Repository -> Retrofit API)
- **Asynchronous**: Kotlin Coroutines
- **State Management**: Compose State (`mutableStateOf`)

```text
app/src/main/java/com/example/queueup/
├── data/
│   ├── model/         # User, Service, QueueEntry, Auth, Dashboard models
│   ├── remote/        # ApiService (Retrofit interface) & ApiClient
│   └── repository/    # AuthRepository, ServiceRepository, QueueRepository, ProfileRepository
├── ui/
│   ├── components/    # QueueCard, ServiceCard, StatCard, CustomTextField
│   ├── screens/       # Splash, Login, Register, Home, ServiceDetail, QueueStatus, QueueHistory, Profile
│   └── theme/         # Color, Theme, Type
├── viewmodel/         # AuthViewModel, HomeViewModel, QueueViewModel, ProfileViewModel
├── notifications/     # NotificationService (Local System Notifications)
├── clevertap/         # Independent Standalone Integration Module
├── utils/             # SessionManager, Constants
└── MainActivity.kt
```

### Backend REST API
- **Runtime**: Node.js + Express
- **Language**: TypeScript
- **Database ORM**: Prisma ORM
- **Database**: SQLite (`file:./dev.db`)
- **Security**: JWT tokens, bcrypt password hashing, role-based authorization middleware (`USER` / `ADMIN`), audit logging.

```text
backend/
├── prisma/
│   ├── schema.prisma  # User, Service, QueueEntry, AuditLog
│   └── dev.db         # Self-contained SQLite database
└── src/
    ├── config/        # Environment configuration
    ├── controllers/   # Auth, Service, Queue, Profile, Dashboard, Content
    ├── middleware/    # Auth, Admin, Error Handler
    ├── routes/        # Auth, Service, Queue, Profile, Dashboard, Content
    ├── services/      # AuthService, QueueService, AuditService
    │   └── clevertap/ # Server-side CleverTap User, Event, Campaign, Report APIs
    ├── types/         # Express & JWT types
    └── server.ts      # Express App Entry & Auto-seeding
```

---

## Database Schema (Prisma / SQLite)

- **User**: `id`, `name`, `email` (unique), `passwordHash`, `occupation`, `interests`, `role` (`USER` | `ADMIN`), `createdAt`, `updatedAt`, `lastActiveAt`
- **Service**: `id`, `name`, `description`, `averageServiceTime` (minutes), `currentWaitingCount`, `isOpen`, `createdAt`, `updatedAt`
- **QueueEntry**: `id`, `userId`, `serviceId`, `position`, `status` (`WAITING` | `SERVED` | `CANCELLED`), `joinedAt`, `servedAt`, `cancelledAt`
- **AuditLog**: `id`, `userId`, `action`, `entity`, `entityId`, `createdAt`

---

## Backend APIs

| Endpoint | Method | Description | Auth Required |
|---|---|---|---|
| `/health` | GET | Server health check | No |
| `/api/auth/register` | POST | Register new user | No |
| `/api/auth/login` | POST | User login | No |
| `/api/auth/me` | GET | Fetch current user | Yes |
| `/api/services` | GET | List available campus services | Yes |
| `/api/services/:id` | GET | Fetch service details | Yes |
| `/api/queues/join` | POST | Join queue token | Yes |
| `/api/queues/my-active` | GET | Fetch active queue entry | Yes |
| `/api/queues/history` | GET | Fetch past queue history | Yes |
| `/api/queues/:id` | GET | Fetch queue entry by ID | Yes |
| `/api/queues/:id/leave` | POST | Cancel active queue token | Yes |
| `/api/queues/:id/refresh` | POST | Refresh active queue status | Yes |
| `/api/profile` | GET | Fetch profile & activity stats | Yes |
| `/api/profile` | PUT | Update profile details | Yes |
| `/api/dashboard` | GET | System overview & metrics | Yes |
| `/api/content/queue-tips` | GET | Campus queue tips | No |

---

## Setup & Running Instructions

### 1. Backend Setup
```bash
cd backend
npm install
npx prisma db push
npm run build
```
Run dev server:
```bash
npm run dev
```
The server starts on port `3000` using SQLite (`file:./dev.db`). It automatically seeds initial campus services (College Administration, Library Help Desk, Student Services, IT Support, Canteen Counter) on first run.

### 2. Android App Build & Run
Open the project in Android Studio or run via Gradle:
```bash
./gradlew assembleDebug
```
Deploy to an emulator or device. The Android app connects to `http://10.0.2.2:3000/api/` for local backend testing on the emulator.

---

## CleverTap Module Isolation Rule

The `clevertap/` package on Android and `backend/src/services/clevertap/` on the server are **standalone integration modules**.  
They are completely decoupled from QueueUp UI, ViewModels, and Core Business Logic.
When CleverTap integration is officially requested, hook calls can be wired in `AuthRepository` and `QueueRepository` in 3 small steps without modifying core screens.
