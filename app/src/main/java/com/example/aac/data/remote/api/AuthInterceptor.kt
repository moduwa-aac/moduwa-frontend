package com.example.aac.data.remote.api

import android.util.Log
import com.example.aac.data.local.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 🔥 runBlocking으로 DataStore에서 항상 최신 토큰을 읽어옵니다.
        val accessToken = tokenProvider.getAccessToken()

        if (!accessToken.isNullOrBlank()) {
            Log.d("NETWORK_AUTH", "🛰️ Request: ${originalRequest.url}")
            Log.d("NETWORK_AUTH", "🔑 Using Token Prefix: ${accessToken.take(15)}...")
        }

        val newRequest = if (!accessToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken") // addHeader 대신 header 사용 (중복 방지)
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
