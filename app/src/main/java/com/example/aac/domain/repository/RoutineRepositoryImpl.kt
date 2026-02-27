package com.example.aac.domain.repository

import com.example.aac.data.mapper.toDomainRoutine
import com.example.aac.data.remote.api.AacApiService
import com.example.aac.data.remote.dto.CreateRoutineRequest
import com.example.aac.data.remote.dto.RoutineUpdateRequest
import com.example.aac.domain.model.Routine
import com.example.aac.domain.repository.RoutineRepository

class RoutineRepositoryImpl(
    private val api: AacApiService
) : RoutineRepository {

    override suspend fun getRoutines(): List<Routine> {
        val res = api.getRoutines()
        if (!res.success) throw IllegalStateException(res.message)

        val data = res.data ?: throw IllegalStateException("getRoutines: data is null")
        return data.routines.map { it.toDomainRoutine() }
    }

    override suspend fun createRoutine(req: CreateRoutineRequest): Routine {
        val res = api.createRoutine(req)
        if (!res.success) throw IllegalStateException(res.message)

        val dto = res.data ?: throw IllegalStateException("createRoutine: data is null")
        return dto.toDomainRoutine()
    }


    override suspend fun updateRoutine(id: String, req: RoutineUpdateRequest): Routine {
        val res = api.updateRoutine(id = id, body = req)
        if (!res.success) throw IllegalStateException(res.message)

        // RoutineUpdateResponse의 data가 nullable이면 여기서도 null 체크 필요
        val routineDto = res.data.routine
        return routineDto.toDomainRoutine()
    }


    override suspend fun deleteRoutine(id: String) {
        TODO("deleteRoutine API 연결 코드 추가")
    }

    override suspend fun deleteAllRoutines() {
        TODO("deleteAllRoutines API 연결 코드 추가")
    }
}
