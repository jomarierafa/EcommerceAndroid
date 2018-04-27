package com.example.jomarie.kotlinpractice.Model

/**
 * Created by jomarie on 4/23/2018.
 */
data class Order(
    val id          : Int,
    var user_id     : Int,
    var transac_num : String,
    var name        : String,
    var itemQuantity: Int,
    var totalAmount : String,
    var ordered_at  : String

)