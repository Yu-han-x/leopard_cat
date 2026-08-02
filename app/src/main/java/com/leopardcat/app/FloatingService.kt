package com.leopardcat.app

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.FrameLayout

class FloatingService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatView: FrameLayout
    private lateinit var catView: ImageView
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isMoving = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // 豹猫主体——圆形占位，后续换成SVG或自定义View
        catView = ImageView(this).apply {
            val drawable = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFFF5A623.toInt())  // 暖金色豹纹底色
                setStroke(3, 0xFFD4841E.toInt()) // 深金边
            }
            background = drawable
        }

        floatView = FrameLayout(this).apply {
            addView(catView, FrameLayout.LayoutParams(120, 120).apply {
                gravity = Gravity.CENTER
            })
        }

        // 悬浮窗参数
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 200
            y = 600
        }

        // 拖拽逻辑
        var isClick = true
        var startClickTime = 0L

        catView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isMoving = false
                    isClick = true
                    startClickTime = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                        isMoving = true
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(floatView, params)
                        isClick = false
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isMoving && System.currentTimeMillis() - startClickTime < 300) {
                        // 点击动画——轻微弹跳
                        catView.animate()
                            .scaleX(0.9f).scaleY(0.9f)
                            .setDuration(80)
                            .withEndAction {
                                catView.animate()
                                    .scaleX(1.1f).scaleY(1.1f)
                                    .setDuration(120)
                                    .withEndAction {
                                        catView.animate()
                                            .scaleX(1f).scaleY(1f)
                                            .setDuration(100)
                                            .start()
                                    }
                                    .start()
                            }
                            .start()
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(floatView, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatView.isInitialized && floatView.windowToken != null) {
            windowManager.removeView(floatView)
        }
    }
}
