package com.example.socialconnect1


data class Notification(
    val id: String,
    val user_id: String,
    val sender_id: String,
    val post_id: String,
    val type: String,
    val seen: Boolean,
    val created_at: String,
    var senderName: String? = null,
    var senderAvatar: String? = null
)