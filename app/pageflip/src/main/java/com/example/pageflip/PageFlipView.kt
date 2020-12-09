package com.example.pageflip

import android.content.Context
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.util.Log
import com.eschao.android.widget.pageflip.PageFlip
import com.eschao.android.widget.pageflip.PageFlipException
import java.util.concurrent.locks.ReentrantLock
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class PageFlipView : GLSurfaceView, GLSurfaceView.Renderer {
    var mPageNo = 0
    var mDuration = 0
    lateinit var mHandler: Handler
    lateinit var mPageFlip: PageFlip
    lateinit var mPageRender: PageRender
    lateinit var mDrawLock: ReentrantLock

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    private fun init(context: Context) {
        // create handler to tackle message
        newHandler()
        mDuration = 1000
        val pixelsOfMesh = 10
        val isAuto = true

        // create PageFlip
        mPageFlip = PageFlip(context)
        mPageFlip.setSemiPerimeterRatio(0.8f)
            .setShadowWidthOfFoldEdges(5f, 60f, 0.3f)
            .setShadowWidthOfFoldBase(5f, 80f, 0.4f)
            .setPixelsOfMesh(pixelsOfMesh)
            .enableAutoPage(false)
        setEGLContextClientVersion(2)

        // init others
        mPageNo = 1
        mDrawLock = ReentrantLock()
        mPageRender = SinglePageRender(
            context, mPageFlip,
            mHandler, mPageNo
        ) { alpha = 0.0f }
        // configure render
        setRenderer(this)
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    /**
     * Handle finger down event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    fun onFingerDown(x: Float, y: Float) {
        // if the animation is going, we should ignore this event to avoid
        // mess drawing on screen
        if (!mPageFlip.isAnimating &&
            mPageFlip.firstPage != null
        ) {
            mPageFlip.onFingerDown(x, y)
        }
    }

    /**
     * Handle finger moving event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    fun onFingerMove(x: Float, y: Float) {
        if (mPageFlip.isAnimating) {
            // nothing to do during animating
        } else if (mPageFlip.canAnimate(x, y)) {
            // if the point is out of current page, try to start animating
            onFingerUp(x, y)
        } else if (mPageFlip.onFingerMove(x, y)) {
            try {
                mDrawLock.lock()
                if (mPageRender.onFingerMove(x, y)) {
                    requestRender()
                }
            } finally {
                mDrawLock.unlock()
            }
        }
    }

    /**
     * Handle finger up event and start animating if need
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    fun onFingerUp(x: Float, y: Float) {
        if (!mPageFlip.isAnimating) {
            mPageFlip.onFingerUp(x, y, mDuration)
            try {
                mDrawLock.lock()
                if (mPageRender.onFingerUp(x, y)) {
                    requestRender()
                }
            } finally {
                mDrawLock.unlock()
            }
        }
    }

    /**
     * Draw frame
     *
     * @param gl OpenGL handle
     */
    override fun onDrawFrame(gl: GL10) {
        try {
            mDrawLock.lock()
            mPageRender.onDrawFrame()
        } finally {
            mDrawLock.unlock()
        }
    }

    /**
     * Handle surface is changed
     *
     * @param gl OpenGL handle
     * @param width new width of surface
     * @param height new height of surface
     */
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        try {
            mPageFlip.onSurfaceChanged(width, height)

            // let page render handle surface change
            mPageRender.onSurfaceChanged(width, height)
        } catch (e: PageFlipException) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceChanged")
        }
    }

    /**
     * Handle surface is created
     *
     * @param gl OpenGL handle
     * @param config EGLConfig object
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        try {
            mPageFlip.onSurfaceCreated()
        } catch (e: PageFlipException) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated")
        }
    }

    /**
     * Create message handler to cope with messages from page render,
     * Page render will send message in GL thread, but we want to handle those
     * messages in main thread that why we need handler here
     */
    private fun newHandler() {
        mHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                when (msg.what) {
                    PageRender.MSG_ENDED_DRAWING_FRAME -> try {
                        mDrawLock!!.lock()
                        // notify page render to handle ended drawing
                        // message
                        if (mPageRender != null &&
                            mPageRender!!.onEndedDrawing(msg.arg1)
                        ) {
                            requestRender()
                        }
                    } finally {
                        mDrawLock!!.unlock()
                    }
                    else -> {
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "PageFlipView"
    }
}