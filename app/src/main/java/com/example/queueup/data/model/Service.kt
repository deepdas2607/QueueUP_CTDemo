// FILE TYPE: Data Model
// PURPOSE: Data representation of a Campus Service/Counter entity.
// USED BY: ServiceRepository, QueueRepository, HomeScreen, ServiceDetailScreen
// DATA SOURCE: Express REST API / Database Service Table

package com.example.queueup.data.model

data class Service(
    val id: String,
    val name: String,
    val description: String,
    val averageServiceTime: Int,
    val currentWaitingCount: Int,
    val isOpen: Boolean = true,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
