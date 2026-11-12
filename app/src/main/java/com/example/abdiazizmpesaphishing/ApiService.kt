package com.example.abdiazizmpesaphishing

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// --- SMS Data Classes ---
data class SmsRequest(val text: String)

data class AnalysisResponse(
    val classification: String,
    val risk_score: Int,
    val suspicious_words: List<String>? = null,
    val explanation: String,
    val recommendation: String
)

// --- URL Data Classes ---
data class UrlRequest(val url: String)

data class UrlAnalysisResponse(
    val classification: String,
    val risk_score: Int,
    val explanation: String,
    val recommendation: String
)

// --- API Interface ---
interface ApiService {
    @POST("api/analyze/sms")
    fun analyzeSms(@Body request: SmsRequest): Call<AnalysisResponse>

    @POST("api/analyze/url")
    fun analyzeUrl(@Body request: UrlRequest): Call<UrlAnalysisResponse>
}