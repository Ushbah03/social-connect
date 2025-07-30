package com.example.socialconnect1.api
import kotlinx.serialization.Serializable

@Serializable
data class RedditResponse(
    val data: RedditData
)

@Serializable
data class RedditData(
    val children: List<RedditChildren>
)

@Serializable
data class RedditChildren(
    val data: RedditPost
)

@Serializable
data class RedditPost(
    val author: String,
    val title: String,
    val thumbnail: String,
    val ups: Int,
    val downs: Int = 0
)
