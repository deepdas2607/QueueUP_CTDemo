# CleverTap Integration Module (QueueUp)

## Overview & Architecture

This package provides a **standalone, production-ready CleverTap integration** for the QueueUp Smart Campus Queue Application.

> **IMPORTANT ARCHITECTURAL RULE:**  
> This module is intentionally decoupled from the QueueUp Core Application UI, ViewModels, and Repositories.  
> The Core QueueUp app functions completely without importing or depending on any classes in this package.

```text
+-------------------------------------------------------+
|                 QueueUp Core Application              |
|  (UI Screens -> ViewModels -> Repositories -> API)    |
+-------------------------------------------------------+
                           |
            [ Future Integration Point ]
                           |
                           v
+-------------------------------------------------------+
|             Standalone CleverTap Module               |
|  CleverTapManager / Events / Profile / Push / Inbox   |
+-------------------------------------------------------+
                           |
                           v
+-------------------------------------------------------+
|                   CleverTap SDK                       |
+-------------------------------------------------------+
```

---

## CleverTap Event Catalog

| Event Name | Trigger Condition | Event Properties |
|---|---|---|
| `User Registered` | Registration succeeds | `userId`, `name`, `email`, `occupation`, `interests` |
| `User Logged In` | User signs in | `userId`, `email` |
| `User Logged Out` | User signs out | `timestamp` |
| `Home Viewed` | User enters Home screen | `activeQueueExists` |
| `Service Viewed` | User selects a campus service | `serviceName`, `serviceId`, `waitingCount` |
| `Queue Joined` | User joins a queue | `serviceName`, `serviceId`, `queuePosition`, `estimatedWaitMinutes` |
| `Queue Position Viewed` | User views active token status | `serviceName`, `queuePosition`, `peopleAhead` |
| `Queue Refreshed` | User taps refresh on active token | `serviceName`, `updatedPosition` |
| `Queue Left` | User cancels active queue token | `serviceName`, `queuePosition` |
| `Queue Completed` | Campus counter serves user | `serviceName`, `waitDurationMinutes` |
| `Consent Updated` | User toggles analytics consent | `analyticsConsent`, `personalizationConsent` |

---

## User Profile Schema

The module pushes the following user properties to CleverTap:

- **Identity**: Backend User ID
- **Name**: Full Name
- **Email**: User Email
- **Occupation**: Student, Faculty, Staff, etc.
- **Interests**: Campus interests
- **Role**: `USER` or `ADMIN`
- **Total Queues Joined**: Numeric Counter
- **Completed Queues**: Numeric Counter
- **Average Wait Minutes**: Calculated wait time metric
- **MSG-push / MSG-email**: Messaging opt-in flags

---

## Identity Lifecycle Management

1. **Anonymous State**: App launches before login; events assigned to anonymous device ID.
2. **Registration / Login**: `CleverTapIdentity.onUserLogin(userId, email, name)` binds past anonymous events to the verified backend `userId`.
3. **Session Restoration**: Session token read from `SessionManager` restores identified session seamlessly.
4. **Logout**: `CleverTapIdentity.onUserLogout()` resets session state.

---

## Phase 2 (P2) Marketing & Engagement Use Cases

### 1. Conversion Funnel Analysis
```text
User Registered -> Service Viewed -> Queue Joined -> Queue Completed
```
Identifies drop-offs between service browsing and joining a queue token.

### 2. Automated Smart Journeys
- **Onboarding Journey**: If `User Registered` is tracked but no `Queue Joined` within 24 hours, trigger a Push Notification / In-App Message recommending popular campus counters.
- **Turn Alert Campaign**: Trigger a Push Notification when `peopleAhead == 1` ("Your turn is coming up in ~3 minutes at Library Help Desk!").

---

## Phase 3 (P3) Server-Side API Architecture

### Server-Side Components (`backend/src/services/clevertap/`)

1. **User API (`userApi.ts`)**: Bulk uploads user profile updates directly from backend background jobs.
2. **Event API (`eventApi.ts`)**: Logs server-verified actions (e.g. `Queue Completed` marked by Admin).
3. **Campaign API (`campaignApi.ts`)**: Triggers transaction push/SMS notifications directly from backend.
4. **Report API (`reportApi.ts`)**: Queries campaign performance & event trends for backend admin reporting.

---

## Future Integration Steps (Connecting to QueueUp)

When manager approval is received to activate CleverTap integration:

1. **Initialize in `MainActivity.kt`**:
   ```kotlin
   CleverTapManager.getInstance().initialize(applicationContext)
   ```
2. **Hook Auth Events in `AuthRepository.kt`**:
   ```kotlin
   CleverTapIdentity.onUserLogin(user.id, user.email, user.name)
   ```
3. **Hook Queue Events in `QueueRepository.kt`**:
   ```kotlin
   CleverTapEvents.trackQueueJoined(serviceName, serviceId, position, estWait)
   ```
