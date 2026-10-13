# QueueUp — Smart Campus Queue Management System

QueueUp is a smart digital queue application designed for campus service counters (College Administration, Library Help Desk, Student Services, IT Support, Canteen Counter, and 7 other campus counters).

The project is built with a **simplified, fail-proof stack** and **textbook MVVM architecture** on Android, designed as a live testbed for the **30-Day CleverTap Training & Debugging Roadmap**.

---

## 1. Architectural Highlights

### Android Core Application (Textbook MVVM)
The Android project is strictly organized into clean, self-explanatory packages so that **Models (M)**, **Views (V)**, and **ViewModels (VM)** can be identified in seconds:

```text
app/src/main/java/com/example/queueup/
├── models/                     # M in MVVM: Plain data classes & payloads
│   ├── UserModels.kt          # User, LoginRequest, RegisterRequest, AuthResponse, UserQueueStats
│   ├── ServiceModel.kt        # Service counter model & QueueTipsResponse
│   ├── QueueModels.kt         # QueueEntry, QueueStatus, ActiveQueueData, JoinQueueRequest
│   └── ActivityLogModel.kt    # ActivityLogEntry, ActivityLogCategory, CampusAnnouncement
│
├── views/                      # V in MVVM: Jetpack Compose UI (Screens, Components, Theme)
│   ├── screens/
│   │   ├── SplashScreen.kt             # Launch screen checking session
│   │   ├── LoginScreen.kt              # User login screen
│   │   ├── RegisterScreen.kt           # User registration screen
│   │   ├── HomeScreen.kt               # Main dashboard with categories & active token
│   │   ├── ServiceDetailScreen.kt      # Counter details & join queue
│   │   ├── QueueStatusScreen.kt        # Live ticket #, position, and wait time
│   │   ├── QueueHistoryScreen.kt       # Past completed & cancelled tickets
│   │   ├── ProfileScreen.kt            # Profile details, stats & preferences
│   │   └── ActivityInspectorScreen.kt  # Live Activity & Payload Inspector screen
│   ├── components/
│   │   ├── QueueCard.kt                # Active queue token card with live turn status
│   │   ├── ServiceCard.kt              # Counter item card with category tag & wait badge
│   │   ├── StatCard.kt                 # Metric summary card for profile
│   │   ├── CustomTextField.kt          # Outlined text field with icons & password toggle
│   │   ├── ErrorBanner.kt              # Clean error banner with dismiss
│   │   └── CampusInboxSheet.kt         # Modal bottom sheet for announcements
│   └── theme/
│       ├── Color.kt                    # Material 3 colors (Light & OLED Dark)
│       ├── Theme.kt                    # Light / Dark theme wrappers
│       └── Type.kt                     # Typography styles
│
├── viewmodels/                 # VM in MVVM: Observable state & business logic
│   ├── AuthViewModel.kt                # Login, registration, and session logic
│   ├── QueueViewModel.kt               # Campus services, joining lines, and turn simulation
│   ├── ProfileViewModel.kt             # Profile editing, queue statistics, and preferences
│   └── ActivityInspectorViewModel.kt   # Live telemetry stream & test event sandbox
│
├── network/                    # Simple API Client & Device Helpers (No complex layers)
│   ├── ApiService.kt                   # Retrofit REST API interface
│   ├── ApiClient.kt                    # OkHttp & Retrofit configuration with emulator fallback
│   ├── SessionManager.kt               # SharedPreferences for JWT token and preferences
│   └── NotificationHelper.kt           # Android system status bar notifications
│
└── MainActivity.kt             # Single Activity hosting Jetpack Compose NavHost
```

---

## 2. Backend Architecture: Simplified Express REST API + Embedded PostgreSQL

The backend uses a **fail-proof, self-contained embedded PostgreSQL engine** (`@electric-sql/pglite`) running directly in-process with zero socket conflicts, zero external cloud database dependencies, and **real SQL tables**:

```text
backend/src/
├── database/
│   └── postgresDatabase.ts    # Embedded PostgreSQL engine, SQL DDL, seeds, and SQL queries
├── controllers/
│   ├── authController.ts      # registerUser, loginUser, getCurrentUser
│   ├── serviceController.ts   # getAllServices, getServiceById, getCampusTips
│   ├── queueController.ts     # joinServiceQueue, getActiveQueueForUser, refreshAndAdvanceQueue, etc.
│   └── profileController.ts   # getUserProfileAndStats, updateUserProfile
├── middleware/
│   └── authMiddleware.ts      # requireAuthToken (JWT verification)
├── routes/
│   └── apiRoutes.ts           # All API routes in one clean file
└── server.ts                  # Express server entry point on port 3000
```

### PostgreSQL SQL Schema
1. **`users`**: `id`, `name`, `email` (unique), `password_hash`, `phone`, `occupation`, `interests`, `role`, `created_at`
2. **`services`**: `id`, `name`, `category`, `description`, `average_service_time`, `current_waiting_count`, `is_open`
3. **`queue_entries`**: `id`, `user_id`, `service_id`, `position`, `status` (`WAITING` | `SERVED` | `CANCELLED`), `joined_at`, `served_at`, `cancelled_at`

---

## 3. Live Activity & Payload Inspector

Accessible directly from the **Terminal icon** on the HomeScreen and ProfileScreen:
- **Real-Time Stream**: Records every user action, identity event, and profile attribute update as formatted JSON.
- **Payload Inspection**: View the exact key-value JSON structure (`Queue_Joined`, `onUserLogin`, `pushProfile`, etc.) that will be delivered when testing CleverTap integrations.
- **Custom Event Test Sandbox**: Type any custom event name and properties on the fly to simulate and inspect payload schemas live.
- **Identity Snapshot**: Preview the currently authenticated user's profile attributes as a JSON block.

---

## 4. How to Run the Project

### 1. Start Backend REST API
```bash
cd backend
npm run build
npm start
```
*Health Check*: `GET http://127.0.0.1:3000/health` $\rightarrow$ `{"status":"ok"}`

### 2. Run Android Application
```bash
# Ensure reverse port tunnel is active so the emulator connects to port 3000:
adb reverse tcp:3000 tcp:3000

# Install and launch debug APK on emulator:
./gradlew installDebug
adb shell am start -n com.example.queueup/.MainActivity
```

**Default Test Login Credentials:**
- Email: `a@b.com`
- Password: `password123`
