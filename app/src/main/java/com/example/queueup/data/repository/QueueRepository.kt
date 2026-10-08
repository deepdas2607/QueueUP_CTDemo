// FILE TYPE: Repository
// PURPOSE: Handles queue operations including joining, active status, refreshing, leaving, and history.
// USED BY: QueueViewModel, HomeViewModel
// DATA SOURCE: ApiService (Express REST API)

package com.example.queueup.data.repository

import com.example.queueup.data.model.ActiveQueueData
import com.example.queueup.data.model.JoinQueueRequest
import com.example.queueup.data.model.QueueEntry
import com.example.queueup.data.remote.ApiClient

class QueueRepository {

    suspend fun joinQueue(serviceId: String): Result<ActiveQueueData> {
        return try {
            val response = ApiClient.apiService.joinQueue(JoinQueueRequest(serviceId))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val activeData = ActiveQueueData(
                    queueEntry = body.queueEntry,
                    peopleAhead = body.peopleAhead,
                    estimatedWaitMinutes = body.estimatedWaitMinutes
                )
                Result.success(activeData)
            } else {
                val rawError = response.errorBody()?.string()
                val errorMsg = com.example.queueup.utils.ErrorParser.parse(rawError)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMyActiveQueue(): Result<ActiveQueueData?> {
        return try {
            val response = ApiClient.apiService.getMyActiveQueue()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.activeQueue)
            } else {
                Result.failure(Exception("Failed to fetch active queue"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun leaveQueue(queueEntryId: String): Result<QueueEntry> {
        return try {
            val response = ApiClient.apiService.leaveQueue(queueEntryId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to leave queue"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshQueue(queueEntryId: String): Result<ActiveQueueData?> {
        return try {
            val response = ApiClient.apiService.refreshQueue(queueEntryId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.activeQueue)
            } else {
                Result.failure(Exception("Failed to refresh queue status"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQueueHistory(): Result<List<QueueEntry>> {
        return try {
            val response = ApiClient.apiService.getQueueHistory()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load queue history"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
