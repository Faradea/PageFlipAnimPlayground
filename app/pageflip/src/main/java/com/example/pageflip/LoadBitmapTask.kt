package com.example.pageflip

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class LoadBitmapTask (context: Context) {

    var mResources: Resources = context.resources

    val bitmap: Bitmap
        get() {
            val resId = R.drawable.screenshot
            return BitmapFactory.decodeResource(mResources, resId)
        }

    companion object {
        private var __object: LoadBitmapTask? = null

        operator fun get(context: Context): LoadBitmapTask? {
            if (__object == null) {
                __object = LoadBitmapTask(context)
            }
            return __object
        }
    }

}