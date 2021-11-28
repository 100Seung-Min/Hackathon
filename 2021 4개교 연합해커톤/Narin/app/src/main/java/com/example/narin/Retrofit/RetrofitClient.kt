package com.example.narin.Retrofit

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create


object RetrofitClient {
    lateinit var api: RetrofitInterface
    init {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        var retrofit = Retrofit.Builder()
            .baseUrl("https://api.smoothbear.me")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        api = retrofit.create(RetrofitInterface::class.java)
    }
}