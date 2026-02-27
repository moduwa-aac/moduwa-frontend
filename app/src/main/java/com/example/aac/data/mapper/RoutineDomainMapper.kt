package com.example.aac.data.mapper

import com.example.aac.data.remote.dto.RoutineDto
import com.example.aac.domain.model.Routine

/**
 * 서버 RoutineDto -> domain Routine 매핑
 * (domain Routine이 실제로 받는 필드만 넣는다)
 */
fun RoutineDto.toDomainRoutine(): Routine {
    return Routine(
        id = id,
        message = message,
        repeatType = repeatType,
        daysOfWeek = daysOfWeek ?: emptyList(),
        daysOfMonth = daysOfMonth ?: emptyList(),
        isMonthEnd = isMonthEnd,
        scheduledTime = scheduledTime,
        isActive = isActive
    )
}
