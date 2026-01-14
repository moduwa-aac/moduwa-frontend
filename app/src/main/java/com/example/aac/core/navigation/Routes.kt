package com.example.aac.core.navigation

import android.net.Uri

object Routes {
    const val AI_SENTENCE = "ai_sentence"

    // ✅ 추가
    const val AI_SENTENCE_EDIT = "ai_sentence_edit"
    const val AI_SENTENCE_EDIT_ROUTE = "$AI_SENTENCE_EDIT?text={text}"

    fun aiSentenceEditRoute(text: String): String =
        "$AI_SENTENCE_EDIT?text=${Uri.encode(text)}"
}
