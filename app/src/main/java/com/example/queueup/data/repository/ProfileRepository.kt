// FILE TYPE: Repository
// PURPOSE: Handles fetching profile, updating user profile, dashboard stats, and tips.
// USED BY: ProfileViewModel, HomeViewModel
// DATA SOURCE: ApiService (Express REST API) & SessionManager

package com.example.queueup.data.repository

import com.example.queueup.data.model.DashboardResponse
import com.example.queueup.data.model.ProfileResponse
import com.example.queueup.data.model.UpdateProfileRequest
import com.example.queueup.data.model.User
import com.example.queueup.data.remote.ApiClient
import com.example.queueup.utils.SessionManager

class ProfileRepository(private val sessionManager: SessionManager) {

    suspend fun getProfile(): Result<ProfileResponse> {
        return try {
            val response = ApiClient.apiService.getProfile()
            if (response.isSuccessful && response.body() != null) {
                val profile = response.body()!!
                sessionManager.saveUser(
                    id = profile.user.id,
                    name = profile.user.name,
                    email = profile.user.email,
                    occupation = profile.user.occupation,
                    interests = profile.user.interests,
                    role = profile.user.role
                )
                Result.success(profile)
            } else {
                Result.failure(Exception("Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfile(name: String?, occupation: String?, interests: String?): Result<User> {
        return try {
            val response = ApiClient.apiService.updateProfile(UpdateProfileRequest(name, occupation, interests))
            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!
                sessionManager.saveUser(
                    id = updatedUser.id,
                    name = updatedUser.name,
                    email = updatedUser.email,
                    occupation = updatedUser.occupation,
                    interests = updatedUser.interests,
                    role = updatedUser.role
                )
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Failed to update profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDashboard(): Result<DashboardResponse> {
        return try {
            val response = ApiClient.apiService.getDashboard()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch dashboard data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQueueTips(): Result<List<String>> {
        return try {
            val response = ApiClient.apiService.getQueueTips()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.tips)
            } else {
                Result.failure(Exception("Failed to load queue tips"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun isNotificationsEnabled(): Boolean = sessionManager.isNotificationsEnabled()
    fun setNotificationsEnabled(enabled: Boolean) = sessionManager.setNotificationsEnabled(enabled)

    fun isPrivacyConsentGiven(): Boolean = sessionManager.isPrivacyConsentGiven()
    fun setPrivacyConsentGiven(given: Boolean) = sessionManager.setPrivacyConsentGiven(given)
}
