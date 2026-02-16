package com.example.aac.data.mapper

import com.example.aac.R

object IconMapper {
    /**
     * 1. UI(Int) -> 서버(String) 변환 (저장/수정할 때 사용)
     */
    fun toRemoteKey(resId: Int): String {
        return when (resId) {
            R.drawable.ic_recent_use -> "ICON_RECENT"   // ✅ 최근사용 추가
            R.drawable.ic_favorite -> "ICON_FAVORITE"   // ✅ 즐겨찾기 추가
            R.drawable.ic_human -> "ICON_PERSON"
            R.drawable.ic_place -> "ICON_PLACE"
            R.drawable.ic_food -> "ICON_FOOD"
            R.drawable.ic_emotion -> "ICON_EMOTION"
            R.drawable.ic_act -> "ICON_ACTION"
            R.drawable.ic_soccer -> "ICON_SOCCER"
            R.drawable.ic_book -> "ICON_BOOK"
            R.drawable.ic_hand -> "ICON_BODY"
            R.drawable.ic_hospital -> "ICON_HOSPITAL"
            R.drawable.ic_pill -> "ICON_PILL"
            R.drawable.ic_school -> "ICON_SCHOOL"
            R.drawable.ic_song -> "ICON_SONG"
            R.drawable.ic_paint -> "ICON_PAINT"
            R.drawable.ic_default -> "ICON_BASIC"
            else -> "ICON_BASIC"
        }
    }

    /**
     * 2. 서버(String) -> UI(Int) 변환 (불러올 때 사용)
     */
    fun toLocalResource(key: String?): Int {
        return when (key) {
            "ICON_RECENT" -> R.drawable.ic_recent_use
            "ICON_FAVORITE" -> R.drawable.ic_favorite
            "ICON_PERSON" -> R.drawable.ic_human
            "ICON_PLACE" -> R.drawable.ic_place
            "ICON_FOOD" -> R.drawable.ic_food
            "ICON_EMOTION" -> R.drawable.ic_emotion
            "ICON_ACTION" -> R.drawable.ic_act
            "ICON_BODY" -> R.drawable.ic_hand
            "ICON_BASIC" -> R.drawable.ic_default

            // 기타 서버 키값들
            "ICON_HOSPITAL" -> R.drawable.ic_hospital
            "ICON_PILL" -> R.drawable.ic_pill
            "ICON_SCHOOL" -> R.drawable.ic_school
            "ICON_SONG" -> R.drawable.ic_song
            "ICON_PAINT" -> R.drawable.ic_paint
            "ICON_SOCCER" -> R.drawable.ic_soccer
            "ICON_BOOK" -> R.drawable.ic_book

            else -> R.drawable.ic_default
        }
    }
}