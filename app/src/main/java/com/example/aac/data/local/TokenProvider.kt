package com.example.aac.data.local

import kotlinx.coroutines.runBlocking

class TokenProvider(
    private val tokenDataStore: TokenDataStore
) {
    // 🔥 메모리 캐싱을 제거하고 항상 DataStore에서 최신 토큰을 읽어옵니다.
    fun getAccessToken(): String? = runBlocking {
        tokenDataStore.getAccessToken()
    }
}
