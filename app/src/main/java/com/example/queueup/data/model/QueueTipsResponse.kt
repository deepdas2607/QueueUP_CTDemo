// FILE TYPE: Data Model
// PURPOSE: Payload for queue tips content demonstration API.
// USED BY: ProfileRepository, HomeViewModel
// DATA SOURCE: Express REST API /api/content/queue-tips

package com.example.queueup.data.model

data class QueueTipsResponse(
    val tips: List<String>
)
