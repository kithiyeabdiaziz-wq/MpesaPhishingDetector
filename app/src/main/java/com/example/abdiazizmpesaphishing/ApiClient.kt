package com.example.abdiazizmpesaphishing

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // TODO: PASTE YOUR LAPTOP IP HERE! Keep the http:// and the :5000/
    private const val BASE_URL = "http://192.168.3.27:5000/"

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}