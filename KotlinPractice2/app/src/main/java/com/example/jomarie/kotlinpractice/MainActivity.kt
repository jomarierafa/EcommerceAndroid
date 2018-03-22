package com.example.jomarie.kotlinpractice


import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState   )
        setContentView(R.layout.activity_main)

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        btnAdd.setOnClickListener {
            val intent = Intent(this, AddProduct::class.java)
            startActivity(intent)

        }

        val btnDisplay = findViewById<Button>(R.id.btnDisplay)
        btnDisplay.setOnClickListener {
            val intent = Intent(this, Display::class.java)
            startActivity(intent)
        }


    }

}
