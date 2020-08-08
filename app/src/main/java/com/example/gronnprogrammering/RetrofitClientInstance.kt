package com.example.gronnprogrammering

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


//Instans av retrofit
/* Denne klassen henter inn basen til API'et slik at vi senere bare trenger kallet på API'et og ikke
hele URL'en hver eneste gang
 */
object RetrofitClientInstance{
    private var retrofit: Retrofit? = null
    private val BASE_URL = "https://in2000-apiproxy.ifi.uio.no/"

    val retrofitInstanse: Retrofit?
        get(){
            if(retrofit == null){
                retrofit = retrofit2.Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
}