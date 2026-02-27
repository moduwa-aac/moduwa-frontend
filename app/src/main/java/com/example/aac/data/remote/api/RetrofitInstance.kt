package com.example.aac.data.remote.api

import android.content.Context
import com.example.aac.data.local.TokenDataStore
import com.example.aac.data.local.TokenProvider
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    private const val BASE_URL = "http://52.78.164.88:3000/"

    lateinit var retrofit: Retrofit
    lateinit var tokenDataStore: TokenDataStore

    fun init(context: Context) {
        tokenDataStore = TokenDataStore(context)
        val tokenProvider = TokenProvider(tokenDataStore)
        val authInterceptor = AuthInterceptor(tokenProvider)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // 🔥 [수정] serializeNulls()를 추가하여 null 필드도 JSON에 포함시킴 (서버의 값 삭제 유도)
        val gson = GsonBuilder()
            .serializeNulls()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val api: AacApiService by lazy {
        if (!::retrofit.isInitialized) {
            throw IllegalStateException("Application 클래스에서 RetrofitInstance.init(context)를 꼭 호출해주세요!")
        }
        retrofit.create(AacApiService::class.java)
    }
}
