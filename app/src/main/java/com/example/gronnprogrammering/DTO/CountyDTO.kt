package com.example.gronnprogrammering.DTO

data class CountyDTO(
    var areacode: Int = 0,
    var areaclass: String,
    var name: String,
    var superareacode: Int = 0,
    var latitude: String,
    var longitude: String){
    var values: MyLocationDTO?= null

}


