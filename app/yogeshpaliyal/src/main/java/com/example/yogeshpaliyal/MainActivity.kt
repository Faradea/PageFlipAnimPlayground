package com.example.yogeshpaliyal

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import techpaliyal.com.curlviewanimation.CurlActivity
import techpaliyal.com.curlviewanimation.CurlView

// https://github.com/yogeshpaliyal/Android-Curl-View-Animation
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val curlView = findViewById<CurlView>(R.id.curlView)

        val images = arrayListOf<Int>()
        images.add(R.drawable.obama)
        images.add(R.drawable.coverpage)
        images.add(R.drawable.coverpage)

        CurlActivity(this).load(curlView, images)
    }
}