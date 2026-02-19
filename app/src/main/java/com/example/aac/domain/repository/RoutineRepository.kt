package com.example.aac.domain.repository

import com.example.aac.data.remote.dto.CreateRoutineRequest
import com.example.aac.data.remote.dto.RoutineUpdateRequest
import com.example.aac.domain.model.Routine

interface RoutineRepository {
    suspend fun getRoutines(): List<Routine>
    suspend fun createRoutine(req: CreateRoutineRequest): Routine
    suspend fun updateRoutine(id: String, req: RoutineUpdateRequest): Routine
    suspend fun deleteRoutine(id: String)
    suspend fun deleteAllRoutines()
}