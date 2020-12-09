package com.example.pageflip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import com.eschao.android.widget.pageflip.OnPageFlipListener
import com.eschao.android.widget.pageflip.PageFlip


/**
 * Abstract Page Render
 *
 * @author eschao
 */
abstract class PageRender(
    var mContext: Context, var mPageFlip: PageFlip,
    handler: Handler, var mPageNo: Int
) :
    OnPageFlipListener {
    var mDrawCommand: Int
    var mBitmap: Bitmap? = null
    var mCanvas: Canvas = Canvas()
    var mBackgroundBitmap: Bitmap? = null
    var mHandler: Handler

    /**
     * Release resources
     */
    fun release() {
        if (mBitmap != null) {
            mBitmap!!.recycle()
            mBitmap = null
        }
        mPageFlip.setListener(null)
        mBackgroundBitmap = null
    }

    /**
     * Handle finger moving event
     *
     * @param x x coordinate of finger moving
     * @param y y coordinate of finger moving
     * @return true if event is handled
     */
    fun onFingerMove(x: Float, y: Float): Boolean {
        mDrawCommand = DRAW_MOVING_FRAME
        return true
    }

    /**
     * Handle finger up event
     *
     * @param x x coordinate of finger up
     * @param y y coordinate of inger up
     * @return true if event is handled
     */
    fun onFingerUp(x: Float, y: Float): Boolean {
        if (mPageFlip.animating()) {
            mDrawCommand = DRAW_ANIMATING_FRAME
            return true
        }
        return false
    }

    /**
     * Render page frame
     */
    abstract fun onDrawFrame()

    /**
     * Handle surface changing event
     *
     * @param width surface width
     * @param height surface height
     */
    abstract fun onSurfaceChanged(width: Int, height: Int)

    /**
     * Handle drawing ended event
     *
     * @param what draw command
     * @return true if render is needed
     */
    abstract fun onEndedDrawing(what: Int): Boolean

    companion object {
        const val MSG_ENDED_DRAWING_FRAME = 1
        private const val TAG = "PageRender"
        const val DRAW_MOVING_FRAME = 0
        const val DRAW_ANIMATING_FRAME = 1
        const val DRAW_FULL_PAGE = 2
        const val MAX_PAGES = 30
    }

    init {
        mDrawCommand = DRAW_FULL_PAGE
        mPageFlip.setListener(this)
        mHandler = handler
    }
}