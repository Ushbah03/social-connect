package com.example.socialconnect1.api

import retrofit2.Call
import retrofit2.http.GET

interface RedditApi {
    @GET("/r/pics/hot.json")
    fun getHotPosts(): Call<RedditResponse>
}
