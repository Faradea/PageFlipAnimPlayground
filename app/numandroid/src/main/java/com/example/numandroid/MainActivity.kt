package com.example.numandroid

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import app.num.numandroidpagecurleffect.PageCurlView

// http://findnerd.com/list/view/Page-Curl-Effect-in-android/21757/?utm_campaign=Submission&utm_medium=Community&utm_source=GrowthHackers.com
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pageCurlView = findViewById<View>(R.id.pagecurl_view) as PageCurlView

        val pages_id: MutableList<Int> = ArrayList()
        pages_id.add(R.drawable.obama)
        pages_id.add(R.drawable.obama)

        pageCurlView.setCurlView(pages_id);
        pageCurlView.setCurlSpeed(100);
    }
}