# CleverTap Standalone Integration Module — QueueUp

## 1. Executive Summary & Architectural Rule

This module contains the **complete, production-ready CleverTap integration** for the QueueUp Smart Campus Queue Application.

> [!IMPORTANT]
> **STRICT ARCHITECTURAL SEPARATION:**  
> This package (`com.example.queueup.clevertap`) and the backend CleverTap services (`backend/src/services/clevertap/`) are **100% decoupled from the QueueUp Core Application**.  
> The Core QueueUp Android app contains **zero** imports of CleverTap classes, runs purely on standard Jetpack Compose + Retrofit + ViewModel + Coroutines, and functions fully without CleverTap being activated.

```text
+-------------------------------------------------------------------------+
|                         QueueUp Core Application                        |
|   (Compose Screens -> ViewModels -> Repositories -> Retrofit REST API)  |
+-------------------------------------------------------------------------+
                                     |
                         [ Future Integration Point ]
                                     |
                                     v
+-------------------------------------------------------------------------+
|                   Standalone CleverTap Integration Module                |
|  CleverTapManager | Events | Profile | Identity | Consent | Push | Inbox  |
+-------------------------------------------------------------------------+
                                     |
                                     v
+-------------------------------------------------------------------------+
|                         CleverTap Android SDK                           |
+-------------------------------------------------------------------------+
```

---

## 2. 30-Day Training Plan Alignment Matrix (Phases P1 – P6)

| Phase | Days | Training Topic | Implementation in this Codebase | Future Connection Hook |
|---|---|---|---|---|
| **P1** | Day 1 | Basic Android App Lifecycle | Clean Android app in `com.example.queueup` with Compose, M3, ViewModel | Core App Ready |
| **P1** | Day 2 | CleverTap Account & Firebase/APNS | `CleverTapConfig.kt`, `CleverTapPush.kt`, Android Notification Channel | `CleverTapPush.registerFcmToken()` |
| **P1** | Day 3 | Profile Creation & Identity, User Properties, Event Schema | `CleverTapIdentity.kt`, `CleverTapProfile.kt`, `CleverTapEvents.kt` | `AuthRepository.kt` on login/register |
| **P1** | Day 4 | Push Templates, In-App & App Inbox | `CleverTapPush.kt`, `CleverTapInApp.kt`, `CleverTapInbox.kt` | TopBar bell icon & FCM receiver |
| **P1** | Day 5 | Web Push, Pop-ups & Exit Intent | Documented web architecture & Server event mapping | Web Portal / Landing page |
| **P2** | Day 6 | Campaigns (Push, In-App, Email) | Documented Campaign triggers & backend Campaign API structure | `CleverTapCampaignApi` |
| **P2** | Day 7 | Multi-step User Journeys | User journey flow models (Onboarding, Wait countdown) | Auto-triggered via CT dashboard |
| **P2** | Day 8 | Segments, RFM & Bulletins | RFM schema tracking: Total Queues, Completed, Frequency | Profile attributes & Event counts |
| **P2** | Day 9 | Analytics, Trends & Cohorts | Granular event logging with timestamps & properties | `CleverTapReportApi` |
| **P2** | Day 10 | Funnels, Flows, Pivots & Lifecycle Optimizer | Conversion funnel models (`Register` -> `View` -> `Join` -> `Served`) | Dashboard analytics ready |
| **P3** | Day 11 | Roles, Access, Audit Logs & Security | Backend `requireAdmin` middleware, `AuditLog` table (`ADMIN_SERVED_QUEUE`) | `AuditService.ts` |
| **P3** | Day 12 | CleverTap Platform Architecture (NB, DB, LP, LC) | Architectural documentation comparing SDK ingestion vs Server ingestion | Section 3 below |
| **P3** | Day 13 | User API & Event API, Campaign & Report API | `userApi.ts`, `eventApi.ts`, `campaignApi.ts`, `reportApi.ts` in backend | Backend Worker / Cron jobs |
| **P3** | Day 14 | Catalog & CSV Upload | `GET /api/content/queue-tips` & campus counter service catalogs | Dashboard catalog sync |
| **P3** | Day 15 | Identity Management (Merge, Conflict, Duplication) | `CleverTapIdentity.kt` stable PostgreSQL UUID resolution | SessionManager & Auth hooks |
| **P4** | Day 16 | Liquid Scripting & Linked Content | Linked Content schema for `/api/content/queue-tips` & personalization | Liquid tags in campaign templates |
| **P4** | Day 17 | Webhook, SMS, WhatsApp Integrations | Server-side event dispatch hooks for multi-channel notification | Backend notification bridge |
| **P4** | Day 18 | Attribution (Google / Facebook) | UTM parameters and campaign attribution placeholders | Intent launcher parser |
| **P4** | Day 19 | Segment.io & Branch Deep Linking | Deep link URI intent filters ready in `MainActivity.kt` | Branch SDK initialization |
| **P4** | Day 20 | GDPR Compliance & Debug APK Testing | `CleverTapConsent.kt` (Opt-in, Opt-out, Data erasure) + verbose SDK logging | Profile toggle |
| **P5-P6**| Day 21-30| Advanced Push, Geofences, Recommendations, Tickets | Architectural models for OEM Push, Geofences around campus, Jira/DevRev | Production scaling |

---

## 3. Architecture: Android SDK vs Backend Server APIs

CleverTap provides two primary ingestion pipelines:

```text
A. Client-Side (Android SDK)                     B. Server-Side (REST APIs)
----------------------------                     --------------------------
Android App                                      Backend Express REST API
     ↓                                                      ↓
CleverTap Android SDK                            CleverTap Server API (v1)
     ↓                                                      ↓
CleverTap Platform                               CleverTap Platform
```

### When to Use Client-Side SDK:
- Real-time user interactions (UI screen views, button clicks, permission prompts).
- Push notification reception & impression tracking.
- Native In-App messages and App Inbox UI rendering.
- Device attributes (OS version, device model, network state, app version).

### When to Use Server-Side APIs:
- Background transactions completed without the app open (e.g. Counter Admin marks a student as `SERVED`).
- Bulk batch uploads of historical user records or student registration imports.
- Transactional alerts triggered by external webhook events.
- Audit-critical actions where client manipulation must be prevented.

---

## 4. CleverTap Event Catalogue

All events are defined in `CleverTapEvents.kt`. Every event supports contextual key-value properties.

| Event Name | When It Fires | Properties | Data Retention / Purpose |
|---|---|---|---|
| `User Registered` | User completes account registration | `userId`, `name`, `email`, `occupation`, `interests` | User activation, welcome journey |
| `User Logged In` | User signs in successfully | `userId`, `email`, `loginMethod` | Session tracking, retention cohort |
| `User Logged Out` | User signs out | `userId`, `timestamp` | Session termination |
| `App Launched` | App comes to foreground | `timestamp` | Daily/Monthly Active Users (DAU/MAU) |
| `Home Viewed` | Home screen displayed | `hasActiveQueue` | Engagement tracking |
| `Service Viewed` | User taps a campus service | `serviceName`, `serviceId`, `currentWaitingCount` | Intent tracking, drop-off analysis |
| `Queue Joined` | User joins a queue ticket | `serviceName`, `serviceId`, `queuePosition`, `estimatedWaitMinutes`, `occupation`, `hourOfDay` | Core conversion event |
| `Queue Position Viewed`| User checks active queue status | `serviceName`, `queuePosition`, `peopleAhead`, `estimatedWaitMinutes` | App utility usage |
| `Queue Refreshed` | User taps refresh on token | `serviceName`, `queuePosition`, `peopleAhead` | Wait behavior analysis |
| `Queue Left` | User cancels active token | `serviceName`, `queuePosition`, `elapsedMinutes` | Abandonment analysis |
| `Queue Completed` | Campus counter serves token | `serviceName`, `waitDurationMinutes` | Completed service funnel |
| `Consent Updated` | User toggles privacy switch | `analyticsConsent`, `personalizationConsent` | GDPR / Privacy compliance |
| `Notification Permission Prompt Shown` | System notification dialog shown | `timestamp` | Permission opt-in funnel |
| `Notification Enabled` | User grants notification permission | `timestamp` | Reachability metric |
| `Notification Disabled`| User revokes notification permission | `timestamp` | Churn risk indicator |
| `First Queue Joined` | Milestone: First queue entry ever | `serviceName` | User milestone campaign |
| `First Queue Completed`| Milestone: First counter service | `serviceName` | Retention milestone |
| `Multiple Queues Joined`| User has joined > 3 queues | `totalCount` | Power user segment / RFM Champion |

---

## 5. User Profile Schema

Stored and pushed via `CleverTapProfile.kt`:

| Profile Property | Type | Description |
|---|---|---|
| `Identity` | String (UUID) | Permanent PostgreSQL User ID (Primary Identifier) |
| `Name` | String | Full user name |
| `Email` | String | User email address (Primary Identifier) |
| `Occupation` | String | Student, Faculty, Staff, Visitor |
| `Interests` | String | Tech, Administration, Library, Canteen |
| `Role` | String | `USER` or `ADMIN` |
| `Total Queues Joined` | Integer | Cumulative count of joined queue tokens |
| `Completed Queues` | Integer | Cumulative count of served queue tokens |
| `Average Waiting Time` | Integer | Average wait time in minutes |
| `Current Queue Status` | String | `WAITING`, `SERVED`, `CANCELLED`, or null |
| `Notifications Enabled`| Boolean | Push notification permission state |
| `Analytics Consent` | Boolean | GDPR tracking consent flag |
| `MSG-push` | Boolean | CleverTap Push opt-in flag |
| `MSG-email` | Boolean | CleverTap Email opt-in flag |

---

## 6. Identity Lifecycle & Conflict Resolution

Handled by `CleverTapIdentity.kt`:

```text
[1. Anonymous Launch]
     Device assigned internal CleverTap ID (GUID)
     Events logged: App Launched, Services Browsed
            ↓
[2. Registration / Login]
     Call: CleverTapIdentity.onUserLogin(userId, email, name)
     CleverTap merges anonymous device history into identified profile
            ↓
[3. Session Persistence]
     Backend JWT & UUID stored in SessionManager
     On app cold restart: CleverTapIdentity.restoreSession(...)
            ↓
[4. Account Switching / Logout]
     Call: CleverTapIdentity.onUserLogout()
     Local state reset to prevent cross-account profile merging
```

---

## 7. Push Notifications & Firebase Architecture

Handled by `CleverTapPush.kt`:

```text
[Firebase Console]
       ↓ (google-services.json + FCM Server Key)
[CleverTap Dashboard]
       ↓ (Campaign Dispatch)
[Firebase Cloud Messaging (FCM)]
       ↓ (Push Message Bundle)
[Device: CleverTapPush.handlePushNotification()]
       ↓ (if fromCleverTap == true)
[Android Notification Channel: "queueup_clevertap_channel"]
```

### Credentials Status:
- **Code Prepared**: Notification channel creation, FCM token registration wrapper, payload inspection, intent handling.
- **Requires Dashboard Configuration**: FCM Private Key / JSON uploaded to CleverTap Dashboard Settings -> Channels -> Push Notifications.

---

## 8. App Inbox & In-App Messaging

- **`CleverTapInbox.kt`**:
  - Initializes App Inbox subsystem.
  - Queries unread count (`getUnreadCount()`).
  - Launches native styled inbox activity (`showAppInbox()`).
  - Supports marking read and deleting messages.
- **`CleverTapInApp.kt`**:
  - Manages in-app message lifecycle (`suspendInAppNotifications()`, `resumeInAppNotifications()`).
  - Intercepts button actions for deep linking (`registerInAppButtonListener()`).

---

## 9. Privacy, Consent & GDPR Compliance

Handled by `CleverTapConsent.kt`:
- **Opt-in / Opt-out**: `cleverTap.setOptOut(!analyticsConsent)`.
- **Right to be Forgotten**: Disassociates identifiers upon account deletion.
- **Data Minimization**: Zero sensitive data (passwords, tokens) is ever passed to CleverTap.

---

## 10. Phase 2 (P2) Analytics & Campaign Use Cases

### A. Conversion Funnels
```text
Step 1: User Registered
Step 2: Service Viewed
Step 3: Queue Joined
Step 4: Queue Completed
```
Enables analyzing drop-off rates at campus counters (e.g. 80% view "College Administration", but only 30% join queue).

### B. Automated Smart Journeys
1. **New Student Onboarding Journey**:
   - Trigger: `User Registered`
   - Delay: 24 Hours
   - Condition: Has `Queue Joined` occurred?
     - `NO` -> Send Push: *"Welcome to campus! Try joining a queue at the Library Help Desk without standing in line."*
     - `YES` -> Exit journey.
2. **Turn Approaching Alert**:
   - Trigger: Backend updates queue position to `#1`
   - Action: Push notification: *"You're up next at Student Services! Please proceed to Counter 2."*

### C. RFM Segmentation
- **Recency**: Last queue joined date.
- **Frequency**: Total queues joined (`totalQueuesJoined >= 5` = Power User).
- **Monetary / Service Time**: High-wait services vs quick transactions.

---

## 11. Phase 3 (P3) Backend Services

Located in `backend/src/services/clevertap/`:

1. **`clevertapService.ts`**: Central HTTP headers and configuration gateway.
2. **`userApi.ts`**: Server-to-server bulk profile uploads.
3. **`eventApi.ts`**: Server-to-server event tracking (e.g. Admin queue actions).
4. **`campaignApi.ts`**: Programmatic campaign triggers via REST API.
5. **`reportApi.ts`**: Metrics reporting API.

---

## 12. Future Step-by-Step Integration Guide

When approval is granted to connect CleverTap to QueueUp:

### Step 1: Initialize in `MainActivity.kt`
```kotlin
// Inside onCreate():
CleverTapManager.getInstance().initialize(applicationContext)
CleverTapPush.createPushNotificationChannel(applicationContext)
```

### Step 2: Hook User Authentication in `AuthRepository.kt`
```kotlin
// In login() / register() onSuccess:
CleverTapIdentity.onUserLogin(user.id, user.email, user.name)
CleverTapProfile.pushUserProfile(
    userId = user.id,
    name = user.name,
    email = user.email,
    occupation = user.occupation,
    interests = user.interests,
    role = user.role
)
```

### Step 3: Hook Queue Actions in `QueueRepository.kt`
```kotlin
// In joinQueue() onSuccess:
CleverTapEvents.trackQueueJoined(
    serviceName = result.queueEntry.service.name,
    serviceId = result.queueEntry.serviceId,
    queuePosition = result.queueEntry.position,
    estimatedWaitMinutes = result.estimatedWaitMinutes
)

// In leaveQueue() onSuccess:
CleverTapEvents.trackQueueLeft(
    serviceName = result.service.name,
    queuePosition = result.position
)
```

**That is all!** Zero rewrites of UI screens or core database models are needed.
