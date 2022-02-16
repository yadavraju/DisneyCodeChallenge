package com.raju.disney.api.repository

import com.raju.disney.api.FlightApi
import com.raju.disney.data.FlightData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlightRepository @Inject constructor(private val api: FlightApi) {

    suspend fun getFlightData(map: MutableMap<String, String>): Flow<FlightData> {
        return object : NetworkBoundRepository<FlightData>() {
            override suspend fun fetchFromRemote(): FlightData = api.getFlight(map)
        }.asFlow()
    }
}