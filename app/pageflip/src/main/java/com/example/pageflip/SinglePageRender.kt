package com.example.pageflip

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Message
import com.eschao.android.widget.pageflip.PageFlip
import com.eschao.android.widget.pageflip.PageFlipState


/**
 * Single page render
 *
 *
 * Every page need 2 texture in single page mode:
 *
 *  * First texture: current page content
 *  * Back texture: back of front content, it is same with first texture
 *
 *  * Second texture: next page content
 *
 *
 *
 * @author eschao
 */
class SinglePageRender(
    context: Context, pageFlip: PageFlip,
    handler: Handler, pageNo: Int
) :
    PageRender(context, pageFlip, handler, pageNo) {
    /**
     * Draw frame
     */
    public override fun onDrawFrame() {
        // 1. delete unused textures
        mPageFlip.deleteUnusedTextures()
        val page = mPageFlip.firstPage

        // 2. handle drawing command triggered from finger moving and animating
        if (mDrawCommand == DRAW_MOVING_FRAME ||
            mDrawCommand == DRAW_ANIMATING_FRAME
        ) {
            // is forward flip
            if (mPageFlip.flipState == PageFlipState.FORWARD_FLIP) {
                // check if second texture of first page is valid, if not,
                // create new one
                if (!page.isSecondTextureSet) {
                    drawPage()
                    page.setSecondTexture(mBitmap)
                }
            } else if (!page.isFirstTextureSet) {
                drawPage()
                page.setFirstTexture(mBitmap)
            }

            // draw frame for page flip
            mPageFlip.drawFlipFrame()
        } else if (mDrawCommand == DRAW_FULL_PAGE) {
            if (!page.isFirstTextureSet) {
                drawPage()
                page.setFirstTexture(mBitmap)
            }
            mPageFlip.drawPageFrame()
        }

        // 3. send message to main thread to notify drawing is ended so that
        // we can continue to calculate next animation frame if need.
        // Remember: the drawing operation is always in GL thread instead of
        // main thread
        val msg = Message.obtain()
        msg.what = MSG_ENDED_DRAWING_FRAME
        msg.arg1 = mDrawCommand
        mHandler.sendMessage(msg)
    }

    /**
     * Handle GL surface is changed
     *
     * @param width surface width
     * @param height surface height
     */
    public override fun onSurfaceChanged(width: Int, height: Int) {
        // recycle bitmap resources if need
        mBackgroundBitmap?.recycle()
        mBitmap?.recycle()

        // create bitmap and canvas for page
        //mBackgroundBitmap = background;
        val page = mPageFlip.firstPage
        mBitmap = Bitmap.createBitmap(
            page.width().toInt(), page.height().toInt(),
            Bitmap.Config.ARGB_8888
        )
        mCanvas?.setBitmap(mBitmap)
        LoadBitmapTask.get(mContext)[width, height] = 1
    }

    /**
     * Handle ended drawing event
     * In here, we only tackle the animation drawing event, If we need to
     * continue requesting render, please return true. Remember this function
     * will be called in main thread
     *
     * @param what event type
     * @return ture if need render again
     */
    public override fun onEndedDrawing(what: Int): Boolean {
        if (what == DRAW_ANIMATING_FRAME) {
            val isAnimating = mPageFlip.animating()
            // continue animating
            return if (isAnimating) {
                mDrawCommand = DRAW_ANIMATING_FRAME
                true
            } else {
                val state = mPageFlip.flipState
                // update page number for backward flip
                if (state == PageFlipState.END_WITH_BACKWARD) {
                    // don't do anything on page number since mPageNo is always
                    // represents the FIRST_TEXTURE no;
                } else if (state == PageFlipState.END_WITH_FORWARD) {
                    mPageFlip.firstPage.setFirstTextureWithSecond()
                    mPageNo++
                }
                mDrawCommand = DRAW_FULL_PAGE
                true
            }
        }
        return false
    }

    /**
     * Draw page content
     */
    private fun drawPage() {
        val width = mCanvas.width
        val height = mCanvas.height
        val p = Paint()
        p.isFilterBitmap = true

        // 1. draw background bitmap
        var background = LoadBitmapTask.get(mContext).bitmap
        val rect = Rect(0, 0, width, height)
        mCanvas.drawBitmap(background!!, null, rect, p)
        background.recycle()
        background = null
    }

    /**
     * If page can flip forward
     *
     * @return true if it can flip forward
     */
    override fun canFlipForward(): Boolean {
        return mPageNo < MAX_PAGES
    }

    /**
     * If page can flip backward
     *
     * @return true if it can flip backward
     */
    override fun canFlipBackward(): Boolean {
        return if (mPageNo > 1) {
            mPageFlip.firstPage.setSecondTextureWithFirst()
            true
        } else {
            false
        }
    }
}