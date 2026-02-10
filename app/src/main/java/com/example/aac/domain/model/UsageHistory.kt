package com.example.aac.domain.model

data class UsageHistory(
    val id: String,
    val sentence: String,
    val date: String,
    val words: List<String>
)
