// FILE TYPE: Data Model
// PURPOSE: System overview and user statistics dashboard response payload.
// USED BY: ProfileRepository, HomeViewModel, ProfileViewModel
// DATA SOURCE: Express REST API /api/dashboard

package com.example.queueup.data.model

data class SystemOverview(
    val totalServices: Int,
    val openServices: Int
)

data class UserStats(
    val totalJoined: Int,
    val completed: Int,
    val cancelled: Int = 0
)

data class DashboardResponse(
    val systemOverview: SystemOverview,
    val hasActiveQueue: Boolean,
    val activeQueue: QueueEntry? = null,
    val userStats: UserStats
)

data class ProfileResponse(
    val user: User,
    val stats: UserStats
)
