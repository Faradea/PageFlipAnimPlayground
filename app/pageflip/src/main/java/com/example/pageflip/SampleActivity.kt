package com.example.pageflip

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView


class SampleActivity : Activity(), GestureDetector.OnGestureListener {

    var mPageFlipView: PageFlipView? = null

    var mGestureDetector: GestureDetector? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val imageView = findViewById<ImageView>(R.id.imageView)
        mPageFlipView = findViewById(R.id.flipView)
        mPageFlipView?.alpha = 0.0f
        mGestureDetector = GestureDetector(this, this)
        this.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    override fun onResume() {
        super.onResume()
        mPageFlipView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mPageFlipView?.onPause()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        Log.d("TouchEvent", "event = $event")
        if (event.action == MotionEvent.ACTION_UP) {
            Log.d("TouchEvent", "onFingerUp")
            mPageFlipView?.onFingerUp(event.x, event.y)
            return true
        }
        return mGestureDetector!!.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.d("TouchEvent", "onFingerDown")
        mPageFlipView?.alpha = 1f
        mPageFlipView?.onFingerDown(e.x, e.y)
        return true
    }

    override fun onFling(
        e1: MotionEvent, e2: MotionEvent, velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d("TouchEvent", "onFling")
        return false
    }

    override fun onLongPress(e: MotionEvent) {
        Log.d("TouchEvent", "onLongPress")
    }

    override fun onScroll(
        e1: MotionEvent, e2: MotionEvent, distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d("TouchEvent", "onFingerMove")
        mPageFlipView?.onFingerMove(e2.x, e2.y)
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        Log.d("TouchEvent", "onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d("TouchEvent", "onSingleTapUp")
        return false
    }
}
