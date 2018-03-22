package com.example.jomarie.kotlinpractice

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.util.*


/**
 * Created by jomarie on 3/21/2018.
 */
public interface ApiInterface {
    @GET("product/showGraphs")
    abstract fun getProductDetails(): Call<ProductResponse>


    companion object Factory {
        val BASE_URL = "http://192.168.1.131:8080/Ecommerce/"
        fun create(): ApiInterface {
            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(ApiInterface::class.java)
        }
    }
}