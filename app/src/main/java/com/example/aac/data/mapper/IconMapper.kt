package com.example.aac.data.mapper

import com.example.aac.R

object IconMapper {
    /**
     * 1. UI(Int) -> 서버(String) 변환 (저장/수정할 때 사용)
     */
    fun toRemoteKey(resId: Int): String {
        return when (resId) {
            R.drawable.ic_recent_use -> "ICON_RECENT"
            R.drawable.ic_favorite -> "ICON_FAVORITE"
            R.drawable.ic_ending -> "ICON_ENDING"
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
            R.drawable.ic_category -> "ICON_BASIC"
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
            "ICON_ENDING" -> R.drawable.ic_ending
            "ICON_PERSON" -> R.drawable.ic_human
            "ICON_PLACE" -> R.drawable.ic_place
            "ICON_FOOD" -> R.drawable.ic_food
            "ICON_EMOTION" -> R.drawable.ic_emotion
            "ICON_ACTION" -> R.drawable.ic_act
            "ICON_BODY" -> R.drawable.ic_hand
            "ICON_BASIC" -> R.drawable.ic_default
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

    /**
     * 3. 🔥 [수정] 모든 카테고리 이름에 대응하도록 매핑 추가
     */
    fun toRemoteKeyByTitle(name: String?): String {
        return when (name?.trim()) {
            "최근사용" -> "ICON_RECENT"
            "즐겨찾기" -> "ICON_FAVORITE"
            "어미" -> "ICON_ENDING"
            "사람" -> "ICON_PERSON"
            "장소" -> "ICON_PLACE"
            "음식" -> "ICON_FOOD"
            "감정" -> "ICON_EMOTION"
            "행동" -> "ICON_ACTION"
            "신체" -> "ICON_BODY"
            "병원" -> "ICON_HOSPITAL"
            "학교" -> "ICON_SCHOOL"
            "기본" -> "ICON_BASIC"
            else -> "ICON_BASIC"
        }
    }

    /**
     * 4. 카테고리 이름 기반으로 바로 로컬 리소스를 찾는 함수
     */
    fun fromCategoryName(name: String?): Int {
        return toLocalResource(toRemoteKeyByTitle(name))
    }
}