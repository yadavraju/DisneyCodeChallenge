package com.raju.disney.api

import com.raju.disney.api.repository.QueryParams.getTraceParent
import com.raju.disney.data.FlightData
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

const val FLIGHT_BASE_URL = "http://192.168.0.102:8080/";

interface FlightApi {

    @GET("flights")
    suspend fun getFlight(@HeaderMap params: MutableMap<String, String>): FlightData
}