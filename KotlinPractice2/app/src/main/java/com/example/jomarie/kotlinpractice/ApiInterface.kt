package com.example.jomarie.kotlinpractice

import com.example.jomarie.kotlinpractice.Activity.Response
import com.example.jomarie.kotlinpractice.Model.CartProduct
import com.example.jomarie.kotlinpractice.Model.Order
import com.example.jomarie.kotlinpractice.Model.OrderedProduct
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
    fun addToCart(@Field("product_id") product_id : Int,
                  @Field("cartCode")   cartCode   : String) : Observable<Response>

    @FormUrlEncoded
    @POST("costumer/showCart")
    fun showCart(@Field("cartCode") cartCode: String): Observable<List<CartProduct>>

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
    fun saveTransaction(@Field("user_id")  user_id : Int,
                        @Field("cartCode") cartCode: String,
                        @Field("name")     name    : String,
                        @Field("email")    email   : String,
                        @Field("contact")  contact : String,
                        @Field("address")  address : String,
                        @Field("cardno")   cardno  : Int,
                        @Field("expiry")   expiry  : String,
                        @Field("cvccode")  cvccode : Int,
                        @Field("quantity") quantity: Int,
                        @Field("amount")   amount  : String) : Observable<Response>

    @FormUrlEncoded
    @POST("costumer/registerUser")
    fun registerUser(@Field("name")        name    : String,
                     @Field("username")    username: String,
                     @Field("password")    password: String,
                     @Field("email")       email   : String,
                     @Field("contact")     contact : String,
                     @Field("address")     address : String,
                     @Field("image")       image   : String) :Observable<Response>

    @FormUrlEncoded
    @POST("costumer/login")
    fun login(@Field("username")   username : String,
              @Field("password")   password : String) : Observable<Response>

    @FormUrlEncoded
    @POST("costumer/updateProfile")
    fun updateProfile(@Field("id")      username : Int,
                      @Field("name")    password : String,
                      @Field("email")   email : String,
                      @Field("contact") contact : String,
                      @Field("address") address: String) : Observable<Response>


    @FormUrlEncoded
    @POST("costumer/getTransaction")
    fun getTransaction(@Field("user_id") user_id : Int) : Observable<List<Order>>

    @FormUrlEncoded
    @POST("costumer/orderedProduct")
    fun getOrderedProduct(@Field("transac_num") transac_num : String) : Observable<List<OrderedProduct>>


    companion object Factory {
        val BASE_URL = "http://192.168.1.110:8080/Ecommerce/"
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