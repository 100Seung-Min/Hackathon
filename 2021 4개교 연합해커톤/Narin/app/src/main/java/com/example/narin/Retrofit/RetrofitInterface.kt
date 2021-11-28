package com.example.narin.Retrofit

import com.example.narin.Data.Message
import com.example.narin.Data.PostChatData
import retrofit2.Call
import retrofit2.http.*

interface RetrofitInterface {
    @POST("/chat")
    fun postChat(
        @Body message: PostChatData
    ): Call<Message>
}