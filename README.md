# QueueUp — Smart Digital Queue Application

QueueUp is a smart digital queue/waiting-list Android application designed for campus service counters (College Administration, Library Help Desk, Student Services, IT Support, Canteen Counter).

The project is built with a strict separation between the **QueueUp Core App** and the **Standalone CleverTap Integration Module**, tailored to support a comprehensive **30-Day CleverTap Training Plan**.

---

## 1. Architectural Highlights

### Two Separate Parts:
1. **QueueUp Core App**:
   - A clean, fully working Android application.
   - Built with **Kotlin, Jetpack Compose, Material 3, Retrofit, OkHttp, ViewModel, Coroutines**.
   - Zero CleverTap imports in UI screens, ViewModels, or core repositories.
   - Ready for standalone demonstration.
2. **Standalone CleverTap Integration Module**:
   - Located in `com.example.queueup.clevertap` (Android) and `backend/src/services/clevertap/` (Backend).
   - Covers **Phases 1 through 3** (and prepared for **Phase 4**).
   - Completely decoupled from QueueUp UI and business logic.
   - Designed to be plugged into QueueUp in 3 minimal hook points when requested.

---

## 2. Database Architecture: Real PostgreSQL (via Embedded PGlite)

The database is **real PostgreSQL**, not SQLite!

In `backend/prisma/schema.prisma`, Prisma is configured with:
```prisma
datasource db {
  provider = "postgresql"
  url      = env("DATABASE_URL")
}
```

To make development effortless without requiring you to install or configure an external PostgreSQL server, the backend includes an embedded local PostgreSQL engine (`@electric-sql/pglite` socket runner via `npm run db:local` or automatically started with `server.ts`) that runs locally on `127.0.0.1:5432`.

### Why this is great:
- **True PostgreSQL Engine**: Satisfies the full project requirement for PostgreSQL, supporting PostgreSQL-native enums (`Role`, `QueueStatus`), relational constraints, and cascade deletes.
- **SQLite-like Portability**: Stores database files locally inside `backend/.pglite-data/`, making the project 100% self-contained with no Docker or external database service needed.
- **Standard PostgreSQL Wire Protocol**: Communicates over standard PostgreSQL wire protocol on port `5432`. Any PostgreSQL client, GUI (pgAdmin, DBeaver), or Prisma CLI connects seamlessly.

---

## 3. Project Structure

### Android Core Application
```text
app/src/main/java/com/example/queueup/
├── data/
│   ├── model/         # User, Service, QueueEntry, AuthResponse, DashboardResponse
│   ├── remote/        # ApiService (Retrofit REST Interface) & ApiClient (OkHttp)
│   └── repository/    # AuthRepository, ServiceRepository, QueueRepository, ProfileRepository
├── ui/
│   ├── components/    # QueueCard, ServiceCard, StatCard, CustomTextField
│   ├── screens/       # Splash, Login, Register, Home, ServiceDetail, QueueStatus, QueueHistory, Profile
│   └── theme/         # Color, Theme, Type (Material 3)
├── viewmodel/         # AuthViewModel, HomeViewModel, QueueViewModel, ProfileViewModel
├── notifications/     # NotificationService (Local System Notifications - NO CleverTap)
├── clevertap/         # Standalone CleverTap Integration Module (Phase 1–3)
│   ├── README.md      # Detailed CleverTap guide & 30-Day Plan mapping
│   ├── CleverTapManager.kt
│   ├── CleverTapEvents.kt
│   ├── CleverTapProfile.kt
│   ├── CleverTapIdentity.kt
│   ├── CleverTapConsent.kt
│   ├── CleverTapPush.kt
│   ├── CleverTapInbox.kt
│   ├── CleverTapInApp.kt
│   └── CleverTapConfig.kt
├── utils/             # SessionManager, Constants
└── MainActivity.kt
```

### Backend REST API
```text
backend/
├── prisma/
│   └── schema.prisma  # PostgreSQL Schema: User, Service, QueueEntry, AuditLog
├── .pglite-data/      # Embedded PostgreSQL data directory
├── src/
│   ├── config/        # Environment configuration
│   ├── controllers/   # Auth, Service, Queue, Profile, Dashboard, Content
│   ├── middleware/    # Auth (JWT), Admin (Role-based), Error Handler
│   ├── routes/        # Auth, Service, Queue, Profile, Dashboard, Content
│   ├── services/      # AuthService, QueueService, AuditService
│   │   └── clevertap/ # Server-side CleverTap User, Event, Campaign, Report APIs
│   │       ├── clevertapService.ts
│   │       ├── userApi.ts
│   │       ├── eventApi.ts
│   │       ├── campaignApi.ts
│   │       └── reportApi.ts
│   ├── types/         # TypeScript interfaces
│   ├── dbLocal.ts     # Embedded PostgreSQL PGlite socket runner
│   └── server.ts      # Express App Entry & Service Auto-seeding
└── package.json
```

---

## 4. Database Schema (PostgreSQL)

- **`User`**: `id` (UUID), `name`, `email` (unique), `passwordHash`, `occupation`, `interests`, `role` (`USER` | `ADMIN`), `createdAt`, `updatedAt`, `lastActiveAt`
- **`Service`**: `id` (UUID), `name`, `description`, `averageServiceTime` (minutes), `currentWaitingCount`, `isOpen`, `createdAt`, `updatedAt`
- **`QueueEntry`**: `id` (UUID), `userId`, `serviceId`, `position`, `status` (`WAITING` | `SERVED` | `CANCELLED`), `joinedAt`, `servedAt`, `cancelledAt`
- **`AuditLog`**: `id` (UUID), `userId`, `action` (`ADMIN_SERVED_QUEUE`, `ADMIN_CANCELLED_QUEUE`, etc.), `entity`, `entityId`, `createdAt`

---

## 5. Backend REST API Endpoints

| Endpoint | Method | Role | Description |
|---|---|---|---|
| `/health` | GET | Public | Server health check |
| `/api/auth/register` | POST | Public | Register new user (case-insensitive email, bcrypt) |
| `/api/auth/login` | POST | Public | User login (returns JWT token) |
| `/api/auth/me` | GET | User | Fetch current authenticated user |
| `/api/services` | GET | User | List all campus services & waiting counts |
| `/api/services/:id` | GET | User | Fetch specific service detail |
| `/api/queues/join` | POST | User | Join queue for a campus counter |
| `/api/queues/my-active` | GET | User | Fetch user's currently active waiting token |
| `/api/queues/history` | GET | User | Fetch user's past queue tickets |
| `/api/queues/:id` | GET | User | Fetch queue entry by ID |
| `/api/queues/:id/leave` | POST | User | Cancel active queue token (`WAITING` -> `CANCELLED`) |
| `/api/queues/:id/refresh` | POST | User | Refresh active queue position & wait time |
| `/api/queues/:id/serve` | POST | Admin | Admin marks queue ticket as `SERVED` (logs `ADMIN_SERVED_QUEUE`) |
| `/api/queues/:id/admin-cancel` | POST | Admin | Admin cancels queue ticket (logs `ADMIN_CANCELLED_QUEUE`) |
| `/api/profile` | GET | User | Fetch profile details & queue statistics |
| `/api/profile` | PUT | User | Update profile name, occupation, interests |
| `/api/dashboard` | GET | User | Get campus queue system metrics |
| `/api/content/queue-tips` | GET | Public | Queue tips catalog (for linked content / Liquid demos) |

---

## 6. Android App Screens (Material 3 + Jetpack Compose)

1. **Splash**: Validates stored JWT session token and routes to Home or Login.
2. **Login**: Email & Password fields with validation, sign in button, and navigation to Register.
3. **Register**: Name, Email, Password, Occupation, and Interests fields with input validation.
4. **Home**: Greeting (`Hello, [Name] 👋`), Active Queue Card (if joined), Queue Tips banner, and List of Available Services.
5. **Service Detail**: Service name, description, currently waiting count, average service time, and "Join Queue Token" button.
6. **Queue Status**: Prominent display of **Your Position (#4)**, **People Ahead (3)**, and **Estimated Wait (18 min)**, with Refresh and Leave Queue actions.
7. **Queue History**: Chronological history of completed (`SERVED`) and cancelled (`CANCELLED`) queue tickets.
8. **Profile & Settings**: User details, queue activity metrics (Total Joined, Completed, Cancelled), Turn Notifications toggle, Privacy Consent toggle, and Logout.

---

## 7. Setup & Running Instructions

### 1. Backend Setup
```bash
cd backend
npm install
npm run build
```

Run the backend:
```bash
npm run dev
# OR for production build:
npm start
```
The server starts:
- Embedded PostgreSQL engine running on `127.0.0.1:5432` (stores data in `backend/.pglite-data/`).
- Express REST API server running on `http://0.0.0.0:3000`.
- Initial campus services are automatically seeded on first launch.

To run migrations or push Prisma schema to local PostgreSQL:
```bash
npx prisma db push
```

### 2. Android App
1. Open the project in **Android Studio**.
2. Select an Android Emulator or connected physical device (Android 7.0+ / API 24+).
3. The Android app connects to `http://10.0.2.2:3000/api/` for emulator testing (or your local IP for physical devices in `Constants.kt`).
4. Click **Run** or run:
```bash
./gradlew assembleDebug
```

---

## 8. Alignment with 30-Day CleverTap Training Plan

The project is structured so you can progress through each phase of the plan while keeping your core application clean:

- **Phase 1 (Day 1–5)**: Clean Android app lifecycle, Firebase notification readiness, CleverTap SDK initialization wrapper (`CleverTapManager.kt`), Identity resolution (`CleverTapIdentity.kt`), Profiles (`CleverTapProfile.kt`), Events catalogue (`CleverTapEvents.kt`), Push & In-App wrappers (`CleverTapPush.kt`, `CleverTapInApp.kt`, `CleverTapInbox.kt`).
- **Phase 2 (Day 6–10)**: Dashboard readiness for Campaigns, Journeys, Segments, RFM, Cohorts, and Conversion Funnels (`User Registered` -> `Service Viewed` -> `Queue Joined` -> `Queue Completed`).
- **Phase 3 (Day 11–15)**: Roles & access (`USER` / `ADMIN`), audit logging (`AuditLog`), platform architecture documentation, Server-side APIs (`userApi.ts`, `eventApi.ts`, `campaignApi.ts`, `reportApi.ts`), and catalog endpoint (`/api/content/queue-tips`).
- **Phase 4 (Day 16–20)**: Personalization via Liquid Scripting with linked content, multi-channel webhook/SMS hooks, and GDPR consent management (`CleverTapConsent.kt`).
- **Phase 5 & 6 (Day 21–30)**: Advanced push handling architecture, OEM notifications, troubleshooting workflows, and support escalation frameworks.

See [`app/src/main/java/com/example/queueup/clevertap/README.md`](file:///Users/deepkumar/Documents/GitHub/QueueUP/app/src/main/java/com/example/queueup/clevertap/README.md) for full CleverTap documentation and the future 3-step connection guide.
