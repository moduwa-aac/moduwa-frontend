package com.example.aac.data.remote.api

import com.example.aac.data.local.TokenProvider
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: TokenProvider
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        // 1. Provider를 통해 토큰 가져오기 (여기서 잠깐 멈춤)
        val accessToken = tokenProvider.getAccessToken()

        // 2. 토큰 있으면 헤더에 추가, 없으면 그냥 보냄
        val newRequest = if (!accessToken.isNullOrBlank()) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}