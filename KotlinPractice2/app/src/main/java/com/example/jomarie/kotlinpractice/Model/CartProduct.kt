package com.example.jomarie.kotlinpractice.Model

/**
 * Created by jomarie on 3/31/2018.
 */
data class CartProduct(val id           : Int,
                       val code         : String,
                       val product      : String,
                       val qty          : Int,
                       val stock        : Int,
                       val amount       : Float,
                       val totalamount  : Float,
                       val image        : String)
