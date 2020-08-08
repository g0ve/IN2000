package com.example.gronnprogrammering.DTO

data class MyLocationDTO(
    val `data`: Data,
    val meta: Meta
)

data class Data(
    val time: List<Time>
)

data class Time(
    val from: String,
    val reason: Reason,
    val to: String,
    val variables: Variables
)

data class Reason(
    val sources: List<Any>,
    val variables: List<Any>
)

data class Variables(
    val AQI: AQI,
    val AQI_no2: AQINo2,
    val AQI_o3: AQIO3,
    val AQI_pm10: AQIPm10,
    val AQI_pm25: AQIPm25,
    val no2_concentration: No2Concentration,
    val o3_concentration: O3Concentration,
    val pm10_concentration: Pm10Concentration,
    val pm25_concentration: Pm25Concentration
)

data class AQIPm25(
    val units: String,
    val value: Double
)

data class No2Concentration(
    val units: String,
    val value: Double
)

data class Pm25Concentration(
    val units: String,
    val value: Double
)

data class AQINo2(
    val units: String,
    val value: Double
)

data class AQIO3(
    val units: String,
    val value: Double
)

data class Pm10Concentration(
    val units: String,
    val value: Double
)

data class AQIPm10(
    val units: String,
    val value: Double
)

data class O3Concentration(
    val units: String,
    val value: Double
)

data class AQI(
    val units: String,
    val value: Double
)

data class Meta(
    val location: Location,
    val reftime: String,
    val sublocations: List<Any>,
    val superlocation: Superlocation
)

data class Superlocation(
    val areaclass: String,
    val areacode: String,
    val latitude: String,
    val longitude: String,
    val name: String,
    val superareacode: String
)

data class Location(
    val areaclass: String,
    val areacode: String,
    val latitude: String,
    val longitude: String,
    val name: String,
    val superareacode: String
)