package com.example.jomarie.kotlinpractice

import com.example.jomarie.kotlinpractice.Activity.Response
import com.example.jomarie.kotlinpractice.Model.CartProduct
import com.example.jomarie.kotlinpractice.Model.Product
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


/**
 * Created by jomarie on 3/21/2018.
 */
interface ApiInterface {
    @FormUrlEncoded
    @POST("costumer/fetch_products")
    fun getProductDetails(@Field("query")    query      : String,
                          @Field("category") category   : String) : Observable<List<Product>>

    @FormUrlEncoded
    @POST("costumer/addToCart")
    fun addToCart(@Field("product_id") product_id : Int) : Observable<Response>

    @GET("costumer/showCart")
    fun showCart(): Observable<List<CartProduct>>

    @FormUrlEncoded
    @POST("costumer/removeFromCart")
    fun removeFromCart(@Field("product_id") product_id  : Int) : Observable<Response>

    @FormUrlEncoded
    @POST("costumer/addItemQuantitys")
    fun addItemQuantity(@Field("qty")          qty         : Int,
                        @Field("quantity")     quantity    : Int,
                        @Field("price")        price       : Float,
                        @Field("code")         code        : String,
                        @Field("stock")        stock       : Int,
                        @Field("product_id")   product_id  : Int,
                        @Field("operation")    operation   : String) : Observable<Response>

    @FormUrlEncoded
    @POST("costumer/addTransaction")
    fun saveTransaction(@Field("name")     name    : String,
                        @Field("email")    email   : String,
                        @Field("contact")  contact : Int,
                        @Field("address")  address : String) : Observable<Response>

    @FormUrlEncoded
    @POST("costumer/registerUser")
    fun registerUser(@Field("name")        name    : String,
                     @Field("username")    username: String,
                     @Field("password")    password: String,
                     @Field("email")       email   : String,
                     @Field("contact")     contact : Int,
                     @Field("address")     address : String) :Observable<Response>

    @FormUrlEncoded
    @POST("costumer/login")
    fun login(@Field("username")   username : String,
              @Field("password")   password : String) : Observable<Response>

    @FormUrlEncoded
    @POST("costumer/updateProfile")
    fun updateProfile(@Field("id")      username : Int,
                      @Field("name")    password : String,
                      @Field("email")   email : String,
                      @Field("contact") contact : Int,
                      @Field("address") address: String) : Observable<Response>


    companion object Factory {
        val BASE_URL = "http://192.168.254.101:8080/Ecommerce/"
        fun create(): ApiInterface {
            val retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(
                            OkHttpClient().newBuilder().addInterceptor (
                                HttpLoggingInterceptor().apply { this.level = HttpLoggingInterceptor.Level.BODY}
                            ).build()
                    )
                    .build()
            return retrofit.create(ApiInterface::class.java)
        }
    }

}