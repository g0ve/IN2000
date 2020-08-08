package com.example.gronnprogrammering

import com.example.gronnprogrammering.DTO.CountyDTO
import com.example.gronnprogrammering.DTO.MyLocationDTO
import com.example.gronnprogrammering.DTO.StationDTO
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface GetCalls {
    @GET("weatherapi/airqualityforecast/0.1/?")
    fun getAllMyLocation(@Query("lat") lat: String, @Query("lon") lon: String) : Call<MyLocationDTO>
    @GET("weatherapi/airqualityforecast/0.1/stations")
    fun getAllStations() : Call<List<StationDTO>>
    @GET("weatherapi/airqualityforecast/0.1/areas?areaclass=kommune")
    fun getAllCounty() : Call<List<CountyDTO>>
}