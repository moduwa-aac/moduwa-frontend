package com.example.aac.core.navigation

import android.net.Uri

object Routes {

    /* ---------- 로그인 ---------- */
    const val LOGIN = "login"

    /* ---------- 약관동의 ---------- */
    const val TERMS = "terms"

    const val TERMS_DETAIL = "terms_detail"
    const val TERMS_DETAIL_ROUTE = "terms_detail/{type}"

    fun termsDetailRoute(type: String) = "terms_detail/$type"

    /* ---------- 메인 ---------- */
    const val MAIN = "main"

    /* ---------- AI 문장 ---------- */
    const val AI_SENTENCE = "ai_sentence"

    /* ---------- AI 문장 편집 ---------- */
    const val AI_SENTENCE_EDIT = "ai_sentence_edit"
    const val AI_SENTENCE_EDIT_ROUTE = "$AI_SENTENCE_EDIT?text={text}"

    fun aiSentenceEditRoute(text: String): String =
        "$AI_SENTENCE_EDIT?text=${Uri.encode(text)}"

    /* ---------- 설정 ---------- */
    const val SETTINGS = "settings"

    /* ---------- 자동 출력 문장 ---------- */

    // 자동 출력 문장 설정(리스트)
    const val AUTO_SENTENCE_SETTING = "auto_sentence_setting"

    // 자동 출력 문장 추가
    const val AUTO_SENTENCE_ADD = "auto_sentence_add"

    // 자동 출력 문장 편집 (serverId 기반)
    const val AUTO_SENTENCE_EDIT = "auto_sentence_edit/{serverId}"

    fun autoSentenceEditRoute(serverId: String): String =
        "auto_sentence_edit/$serverId"

    // 자동 출력 문장 선택 삭제 화면
    const val AUTO_SENTENCE_SELECT_DELETE = "auto_sentence_select_delete"

    // 목소리 설정 화면
    const val VOICE_SETTING = "voice_setting"

    const val CATEGORY_MANAGEMENT = "category_management"

    const val SPEAK_SETTING = "speak_setting"

}
