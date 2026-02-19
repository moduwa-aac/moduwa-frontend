package com.example.aac.data.mapper

import com.example.aac.data.remote.dto.RoutineDto
import com.example.aac.ui.features.auto_sentence.AutoSentenceItem
import com.example.aac.ui.features.auto_sentence.repeat.RepeatSetting
import com.example.aac.ui.features.auto_sentence.repeat.RepeatType
import com.example.aac.ui.features.auto_sentence.repeat.Weekday
import com.example.aac.ui.features.auto_sentence.time.TimeState
import kotlin.math.abs
import com.example.aac.data.remote.dto.RoutineUpdateRequest

/**
 * 서버 RoutineDto -> UI AutoSentenceItem 매핑
 * - serverId: 서버 UUID(String) 그대로 보관 (PATCH/DELETE용)
 * - id: UI에서 쓰는 안정 Long (hash 기반)
 */
fun RoutineDto.toAutoSentenceItem(): AutoSentenceItem {
    return AutoSentenceItem(
        id = id.toStableLongId(),
        serverId = id, // ✅ 서버 id(UUID)
        sentence = message,
        repeatSetting = this.toRepeatSetting(),
        timeState = scheduledTime.toTimeState()
    )
}

/** UUID(String) -> Long (UI용) */
private fun String.toStableLongId(): Long = abs(this.hashCode().toLong())

/** "08:30" -> TimeState(isAm, hour(1~12), minute) */
private fun String.toTimeState(): TimeState {
    val parts = this.split(":")
    val hour24 = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val isAm = hour24 < 12
    val hour12 = when (hour24 % 12) {
        0 -> 12
        else -> hour24 % 12
    }

    return TimeState(
        isAm = isAm,
        hour = hour12,
        minute = minute
    )
}

/**
 * 서버 repeatType/daysOfWeek/daysOfMonth -> RepeatSetting 변환
 * - daysOfWeek/daysOfMonth는 null 올 수 있어 안전처리
 * - isMonthEnd는 UI RepeatSetting에 없어서 현재는 반영 안 함
 */
private fun RoutineDto.toRepeatSetting(): RepeatSetting {
    val repeatTypeUi: RepeatType = when (repeatType.uppercase()) {
        "DAILY" -> RepeatType.DAILY
        "WEEKLY" -> RepeatType.WEEKLY
        "BIWEEKLY" -> RepeatType.BIWEEKLY
        "MONTHLY" -> RepeatType.MONTHLY
        else -> RepeatType.WEEKLY
    }

    val weekdaysUi: Set<Weekday> = (daysOfWeek ?: emptyList())
        .mapNotNull { it.toWeekdayOrNull() }
        .toSet()

    val monthDaysUi: Set<Int> = (daysOfMonth ?: emptyList())
        .filter { it in 1..31 }
        .toSet()

    return RepeatSetting(
        type = repeatTypeUi,
        days = weekdaysUi,
        monthDays = monthDaysUi
    )
}

/** 서버 daysOfWeek Int -> Weekday (가정: 1=Mon ... 7=Sun) */
private fun Int.toWeekdayOrNull(): Weekday? {
    return when (this) {
        1 -> Weekday.MON
        2 -> Weekday.TUE
        3 -> Weekday.WED
        4 -> Weekday.THU
        5 -> Weekday.FRI
        6 -> Weekday.SAT
        7 -> Weekday.SUN
        else -> null
    }
}

/**
 * UI AutoSentenceItem -> 서버 RoutineUpdateRequest 변환 (PATCH용)
 * - WEEKLY/BIWEEKLY: daysOfMonth는 null로 보내서 JSON에서 빠지게 함
 * - MONTHLY: daysOfWeek는 null로 보내서 JSON에서 빠지게 함
 * - MONTHLY에서 31은 "말일"로 쓸 수도 있어서 isMonthEnd로 분리
 */
fun AutoSentenceItem.toRoutineUpdateRequest(): RoutineUpdateRequest {
    val type = repeatSetting.type.name // DAILY/WEEKLY/BIWEEKLY/MONTHLY

    // ✅ 요일: 주/격주만 보냄 (그 외는 null)
    val daysOfWeek: List<Int>? =
        if (type == "WEEKLY" || type == "BIWEEKLY") repeatSetting.days.toServerDaysOfWeek()
        else null

    // ✅ 말일 처리(31을 말일로 쓰는 케이스)
    val isMonthEnd = (type == "MONTHLY") && repeatSetting.monthDays.contains(31)

    // ✅ 날짜: 월간만 보냄 (그 외는 null)
    // - 31을 말일로 쓰면 서버가 따로 받는다면 daysOfMonth에서는 빼주는 게 안전
    val daysOfMonth: List<Int>? =
        if (type == "MONTHLY") {
            val filtered = repeatSetting.monthDays
                .filter { it in 1..31 }
                .filterNot { it == 31 && isMonthEnd }
                .toList()

            // 서버가 "최소 1개" 강제면, 말일(isMonthEnd=true)만 선택한 경우 filtered가 비게 됨
            // -> 그땐 null 대신 emptyList()가 아니라, isMonthEnd=true로 보내고 daysOfMonth는 null로 빼는 게 보통 맞음
            // (서버가 isMonthEnd만으로 허용해야 정상)
            if (filtered.isEmpty() && !isMonthEnd) null else filtered
        } else null

    return RoutineUpdateRequest(
        message = sentence,
        scheduledTime = timeState.toServerTimeString(),
        repeatType = type,
        daysOfWeek = daysOfWeek,
        daysOfMonth = daysOfMonth,
        isMonthEnd = isMonthEnd
    )
}

private fun TimeState.toServerTimeString(): String {
    val hour24 = if (isAm) {
        if (hour == 12) 0 else hour
    } else {
        if (hour == 12) 12 else hour + 12
    }
    return "%02d:%02d".format(hour24, minute)
}

private fun Set<Weekday>.toServerDaysOfWeek(): List<Int> {
    return this.map {
        when (it) {
            Weekday.MON -> 1
            Weekday.TUE -> 2
            Weekday.WED -> 3
            Weekday.THU -> 4
            Weekday.FRI -> 5
            Weekday.SAT -> 6
            Weekday.SUN -> 7
        }
    }.sorted()
}
