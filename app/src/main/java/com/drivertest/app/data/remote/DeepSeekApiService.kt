package com.drivertest.app.data.remote

import com.drivertest.app.data.remote.dto.DeepSeekRequest
import com.drivertest.app.data.remote.dto.DeepSeekResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DeepSeekApiService {

    @POST("chat/completions")
    suspend fun chatCompletion(@Body request: DeepSeekRequest): DeepSeekResponse
}
