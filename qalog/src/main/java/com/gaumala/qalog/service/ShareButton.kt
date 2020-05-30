package com.gaumala.qalog.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.MotionEvent.*
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.gaumala.qalog.R
import kotlin.math.abs
import kotlin.math.max

class ShareButton(ctx: Context) {
    private val view = createView(ctx)
    private val maxClickDistance = dpToPx(ctx, 3)
    private val maxClickDuration = 200L

    fun attach(ctx: Context, wm: WindowManager) {
        val params = createLayoutParams(ctx)

        val touchListener = createOnTouchListener { newX, newY ->
            params.x = newX.toInt()
            params.y = newY.toInt()

            wm.updateViewLayout(view, params)
        }
        view.setOnTouchListener(touchListener)

        wm.addView(view, params)
    }

    var clickListener = { -> }

    private fun createOnTouchListener(updatePosition: (Float, Float) -> Unit): View.OnTouchListener {
        return object: View.OnTouchListener {
            var xRef = 0f
            var yRef = 0f
            var xStart = 0f
            var yStart = 0f
            var touchBeganAt = 0L
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(view: View, e: MotionEvent): Boolean {
                when (e.action) {
                    ACTION_DOWN -> {
                        val params = view.layoutParams as WindowManager.LayoutParams
                        xRef = params.x.toFloat()
                        yRef = params.y.toFloat()
                        xStart = e.rawX
                        yStart = e.rawY
                        touchBeganAt = System.currentTimeMillis()
                    }

                    ACTION_MOVE -> {
                        val xDiff = e.rawX - xStart
                        val yDiff = e.rawY - yStart
                        updatePosition(xRef - xDiff, yRef + yDiff)


                    }

                    ACTION_UP -> {
                        val distanceDiff = max(abs(e.rawX - xStart), abs(e.rawY - yStart))
                        val timeDiff = System.currentTimeMillis() - touchBeganAt
                        Log.d("QALogService", "ACTION_UP $timeDiff / $maxClickDuration || $distanceDiff / $maxClickDistance")
                        if (timeDiff < maxClickDuration
                            && distanceDiff < maxClickDistance)
                            clickListener()
                    }
                }

                return true
            }


        }
    }

    private fun createLayoutParams(ctx: Context): WindowManager.LayoutParams {
        @Suppress("DEPRECATION") val type =
            if (Build.VERSION.SDK_INT < 26)
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY

        val size = dpToPx(ctx, 56).toInt()
        val params = WindowManager.LayoutParams(
            size, size,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.END
        return params
    }

    fun detach(wm: WindowManager) {
        wm.removeView(view)
    }

    private companion object {
        private fun dpToPx(ctx: Context, dps: Int): Float =
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dps.toFloat(), ctx.resources.displayMetrics)

        fun createView(ctx: Context): View {
            val view = ImageView(ctx)
            val padding = dpToPx(ctx, 16).toInt()
            view.setImageResource(R.drawable.ic_qa_log_share_white_24dp)
            view.setPadding(padding, padding, padding, padding)
            view.background = ContextCompat.getDrawable(ctx, R.drawable.qa_log_btn_bg)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.elevation = dpToPx(ctx, 4)
            }

            return view
        }
    }
}