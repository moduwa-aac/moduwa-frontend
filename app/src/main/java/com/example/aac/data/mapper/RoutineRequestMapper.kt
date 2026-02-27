package com.example.aac.data.mapper

import com.example.aac.data.remote.dto.CreateRoutineRequest
import com.example.aac.ui.features.auto_sentence.AutoSentenceItem
import com.example.aac.ui.features.auto_sentence.repeat.RepeatType
import com.example.aac.ui.features.auto_sentence.repeat.Weekday
import com.example.aac.ui.features.auto_sentence.time.TimeState

fun AutoSentenceItem.toCreateRoutineRequest(): CreateRoutineRequest {
    val scheduledTime = timeState.toScheduledTimeString()

    val daysOfWeek = when (repeatSetting.type) {
        RepeatType.WEEKLY, RepeatType.BIWEEKLY -> repeatSetting.days.map { it.toServerDayOfWeek() }
        else -> emptyList()
    }

    val daysOfMonth = when (repeatSetting.type) {
        RepeatType.MONTHLY -> repeatSetting.monthDays.toList()
        else -> emptyList()
    }

    val isMonthEnd = repeatSetting.type == RepeatType.MONTHLY && repeatSetting.monthDays.contains(31)

    return CreateRoutineRequest(
        message = sentence,
        scheduledTime = scheduledTime,
        repeatType = repeatSetting.type.name, // DAILY/WEEKLY/BIWEEKLY/MONTHLY
        daysOfWeek = daysOfWeek,
        daysOfMonth = daysOfMonth.filter { it in 1..31 },
        isMonthEnd = isMonthEnd
    )
}

private fun TimeState.toScheduledTimeString(): String {
    val hour24 = when {
        isAm && hour == 12 -> 0
        !isAm && hour == 12 -> 12
        !isAm -> hour + 12
        else -> hour
    }
    return "${hour24.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
}

private fun Weekday.toServerDayOfWeek(): Int {
    return when (this) {
        Weekday.MON -> 1
        Weekday.TUE -> 2
        Weekday.WED -> 3
        Weekday.THU -> 4
        Weekday.FRI -> 5
        Weekday.SAT -> 6
        Weekday.SUN -> 7
    }
}
