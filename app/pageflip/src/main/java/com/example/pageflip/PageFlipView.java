package com.example.pageflip;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.SyncStateContract;
import android.util.AttributeSet;
import android.util.Log;

import com.eschao.android.widget.pageflip.PageFlip;
import com.eschao.android.widget.pageflip.PageFlipException;

import java.util.concurrent.locks.ReentrantLock;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class PageFlipView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private final static String TAG = "PageFlipView";

    int mPageNo;
    int mDuration;
    Handler mHandler;
    PageFlip mPageFlip;
    PageRender mPageRender;
    ReentrantLock mDrawLock;

    public PageFlipView(Context context) {
        super(context);
        init(context);
    }

    public PageFlipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        // create handler to tackle message
        newHandler();

        mDuration = 1000;
        int pixelsOfMesh = 10;
        boolean isAuto = true;

        // create PageFlip
        mPageFlip = new PageFlip(context);
        mPageFlip.setSemiPerimeterRatio(0.8f)
                .setShadowWidthOfFoldEdges(5, 60, 0.3f)
                .setShadowWidthOfFoldBase(5, 80, 0.4f)
                .setPixelsOfMesh(pixelsOfMesh)
                .enableAutoPage(false);
        setEGLContextClientVersion(2);

        // init others
        mPageNo = 1;
        mDrawLock = new ReentrantLock();
        mPageRender = new SinglePageRender(context, mPageFlip,
                mHandler, mPageNo);
        // configure render
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /**
     * Handle finger down event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerDown(float x, float y) {
        // if the animation is going, we should ignore this event to avoid
        // mess drawing on screen
        if (!mPageFlip.isAnimating() &&
                mPageFlip.getFirstPage() != null) {
            mPageFlip.onFingerDown(x, y);
        }
    }

    /**
     * Handle finger moving event
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerMove(float x, float y) {
        if (mPageFlip.isAnimating()) {
            // nothing to do during animating
        }
        else if (mPageFlip.canAnimate(x, y)) {
            // if the point is out of current page, try to start animating
            onFingerUp(x, y);
        }
        // move page by finger
        else if (mPageFlip.onFingerMove(x, y)) {
            try {
                mDrawLock.lock();
                if (mPageRender != null &&
                        mPageRender.onFingerMove(x, y)) {
                    requestRender();
                }
            }
            finally {
                mDrawLock.unlock();
            }
        }
    }

    /**
     * Handle finger up event and start animating if need
     *
     * @param x finger x coordinate
     * @param y finger y coordinate
     */
    public void onFingerUp(float x, float y) {
        if (!mPageFlip.isAnimating()) {
            mPageFlip.onFingerUp(x, y, mDuration);
            try {
                mDrawLock.lock();
                if (mPageRender != null &&
                        mPageRender.onFingerUp(x, y)) {
                    requestRender();
                }
            }
            finally {
                mDrawLock.unlock();
            }
        }
    }

    /**
     * Draw frame
     *
     * @param gl OpenGL handle
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        try {
            mDrawLock.lock();
            if (mPageRender != null) {
                mPageRender.onDrawFrame();
            }
        }
        finally {
            mDrawLock.unlock();
        }
    }

    /**
     * Handle surface is changed
     *
     * @param gl OpenGL handle
     * @param width new width of surface
     * @param height new height of surface
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        try {
            mPageFlip.onSurfaceChanged(width, height);

            // if there is the second page, create double page render when need
            int pageNo = mPageRender.getPageNo();
            if (mPageFlip.getSecondPage() != null && width > height) {
                if (!(mPageRender instanceof DoublePagesRender)) {
                    mPageRender.release();
                    mPageRender = new DoublePagesRender(getContext(),
                            mPageFlip,
                            mHandler,
                            pageNo);
                }
            }
            // if there is only one page, create single page render when need
            else if(!(mPageRender instanceof SinglePageRender)) {
                mPageRender.release();
                mPageRender = new SinglePageRender(getContext(),
                        mPageFlip,
                        mHandler,
                        pageNo);
            }

            // let page render handle surface change
            mPageRender.onSurfaceChanged(width, height);
        }
        catch (PageFlipException e) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceChanged");
        }
    }

    /**
     * Handle surface is created
     *
     * @param gl OpenGL handle
     * @param config EGLConfig object
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        try {
            mPageFlip.onSurfaceCreated();
        }
        catch (PageFlipException e) {
            Log.e(TAG, "Failed to run PageFlipFlipRender:onSurfaceCreated");
        }
    }

    /**
     * Create message handler to cope with messages from page render,
     * Page render will send message in GL thread, but we want to handle those
     * messages in main thread that why we need handler here
     */
    private void newHandler() {
        mHandler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case PageRender.MSG_ENDED_DRAWING_FRAME:
                        try {
                            mDrawLock.lock();
                            // notify page render to handle ended drawing
                            // message
                            if (mPageRender != null &&
                                    mPageRender.onEndedDrawing(msg.arg1)) {
                                requestRender();
                            }
                        }
                        finally {
                            mDrawLock.unlock();
                        }
                        break;

                    default:
                        break;
                }
            }
        };
    }
}