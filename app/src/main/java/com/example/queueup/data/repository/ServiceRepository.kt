// FILE TYPE: Repository
// PURPOSE: Handles fetching campus services and service details.
// USED BY: HomeViewModel, QueueViewModel
// DATA SOURCE: ApiService (Express REST API)

package com.example.queueup.data.repository

import com.example.queueup.data.model.Service
import com.example.queueup.data.remote.ApiClient

class ServiceRepository {

    suspend fun getServices(): Result<List<Service>> {
        return try {
            val response = ApiClient.apiService.getServices()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to load services"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getServiceById(id: String): Result<Service> {
        return try {
            val response = ApiClient.apiService.getServiceById(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Service not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
