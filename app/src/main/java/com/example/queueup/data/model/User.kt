// FILE TYPE: Data Model
// PURPOSE: Data representation of a User entity.
// USED BY: AuthRepository, ProfileRepository, ViewModels
// DATA SOURCE: Express REST API / Database User Table

package com.example.queueup.data.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val occupation: String? = null,
    val interests: String? = null,
    val role: String = "USER",
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastActiveAt: String? = null
)
