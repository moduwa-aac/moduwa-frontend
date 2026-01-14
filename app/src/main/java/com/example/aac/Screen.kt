package com.example.aac

import android.net.Uri // ✅ 이거 추가

sealed class Screen(val route: String) {
    data object AiSentenceComplete : Screen("ai_sentence_complete")

    data object AiSentenceEdit : Screen("ai_sentence_edit?text={text}") {
        fun createRoute(text: String): String =
            "ai_sentence_edit?text=${Uri.encode(text)}"
    }
}
