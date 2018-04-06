package com.example.jomarie.kotlinpractice

import com.example.jomarie.kotlinpractice.Activity.ProductResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


/**
 * Created by jomarie on 3/21/2018.
 */
public interface ApiInterface {
    @FormUrlEncoded
    @POST("costumer/fetch_product")
    abstract fun getProductDetails(@Field("query") query: String): Call<ProductResponse>
    @FormUrlEncoded
    @POST("costumer/addToCart")
    abstract fun addToCart(@Field("product_id") product_id :Int): Call<ProductResponse>

    @GET("costumer/showCarts")
    abstract fun showCart(): Call<ProductResponse>

    @FormUrlEncoded
    @POST("costumer/removeFromCart")
    abstract fun removeFromCart(@Field("product_id") product_id: Int) : Call<ProductResponse>

    @FormUrlEncoded
    @POST("costumer/addItemQuantitys")
    abstract fun addItemQuantity(@Field("qty")          qty         : Int,
                                 @Field("quantity")     quantity    : Int,
                                 @Field("price")        price       : Int,
                                 @Field("code")         code        : String,
                                 @Field("stock")        stock       : Int,
                                 @Field("product_id")   product_id  : Int,
                                 @Field("operation")    operation   : String) : Call<ProductResponse>

    @FormUrlEncoded
    @POST("costumer/addTransaction")
    abstract fun saveTransaction(@Field("name")     name    : String,
                                 @Field("email")    email   : String,
                                 @Field("contact")  contact : Int,
                                 @Field("address")  address : String) : Call<ProductResponse>

    @FormUrlEncoded
    @POST("costumer/registerUser")
    abstract fun registerUser(@Field("name")        name    : String,
                              @Field("username")    username: String,
                              @Field("password")    password: String,
                              @Field("email")       email   : String,
                              @Field("contact")     contact : Int,
                              @Field("address")     address : String) :Call<ProductResponse>

    @FormUrlEncoded
    @POST("costumer/login")
    abstract fun login(@Field("username")   username : String,
                       @Field("password")   password : String) : Call<ProductResponse>

    companion object Factory {
        val BASE_URL = "http://192.168.15.84:8080/Ecommerce/"
        fun create(): ApiInterface {
            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            return retrofit.create(ApiInterface::class.java)
        }
    }
}