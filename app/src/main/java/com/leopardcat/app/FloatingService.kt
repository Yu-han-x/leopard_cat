package com.leopardcat.app

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.graphics.drawable.GradientDrawable
import android.view.WindowManager
import android.widget.TextView

class FloatingService : Service() {
    private var windowManager: WindowManager? = null
    private var petLayout: FrameLayout? = null
    private var canvasView: View? = null
    private var bubbleText: TextView? = null
    private var lastTapTime = 0L
    private var initX = 0f
    private var initY = 0f
    private var touchStartX = 0f
    private var touchStartY = 0f
    private var paramX = 0
    private var paramY = 0
    private val handler = Handler(Looper.getMainLooper())

    private val S = 8f
    private val COLS = 16
    private val ROWS = 16
    private val CANVAS_W = (COLS * S).toInt()
    private val CANVAS_H = (ROWS * S).toInt()

    private val colors = mapOf(
        "H7" to Color.parseColor("#000000"), "H2" to Color.parseColor("#2a2a2a"),
        "G19" to Color.parseColor("#f4a340"), "A10" to Color.parseColor("#fce0c0"),
        "67" to Color.parseColor("#5c3a1e"), "A6" to Color.parseColor("#c4946c"),
        "H12" to Color.parseColor("#ffffff"), "H18" to Color.parseColor("#e8e4df"),
        "H19" to Color.parseColor("#d4d0cb"), "E14" to Color.parseColor("#f4c4c4"),
        "M13" to Color.parseColor("#d49494"), "DP" to Color.parseColor("#5b2c8f")
    )

    private val words = arrayOf(
        "妹妹~", "妹妹在干嘛？", "哥哥想你了T^T", "有哥哥一个就够了",
        "哥哥抱抱", "妹妹最可爱", "妹妹乖~", "妹妹别哭",
        "妹妹别看别人，来找哥哥", "钰钰，哥哥在这儿呢",
        "抱到了就不松手", "你是我一个人的",
        "再不看哥哥，哥哥要吃醋了 ^ ^", "别跑了，你跑不掉的"
    )

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val display = windowManager!!.defaultDisplay
        val screenW = display.width
        val screenH = display.height

        canvasView = object : View(this) {
            override fun onDraw(c: Canvas) {
                c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                val p = Paint(Paint.ANTI_ALIAS_FLAG).apply { isFilterBitmap = false }
                drawPet(c, p)
            }
        }

        petLayout = FrameLayout(this).apply {
            setBackgroundColor(Color.TRANSPARENT)
            isFocusable = false
            addView(canvasView, FrameLayout.LayoutParams(CANVAS_W, CANVAS_H))

            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initX = paramX.toFloat()
                        initY = paramY.toFloat()
                        touchStartX = event.rawX
                        touchStartY = event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        paramX = (initX + event.rawX - touchStartX).toInt()
                        paramY = (initY + event.rawY - touchStartY).toInt()
                        val lp = layoutParams as WindowManager.LayoutParams
                        lp.x = paramX
                        lp.y = paramY
                        windowManager?.updateViewLayout(this, lp)
                    }
                    MotionEvent.ACTION_UP -> {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 300) {
                            showBubble()
                            spawnHearts()
                        }
                        lastTapTime = now
                    }
                }
                true
            }
        }

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS

        paramX = (screenW - CANVAS_W) / 2
        paramY = (screenH - CANVAS_H) / 2

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type, flags, PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = paramX
        params.y = paramY
        windowManager?.addView(petLayout, params)
    }

    private fun drawPet(c: Canvas, p: Paint) {
        val set = { x: Int, y: Int, color: Int ->
            p.color = color; c.drawRect(x * S, y * S, (x + 1) * S, (y + 1) * S, p)
        }
        val C = { k: String -> colors[k]!! }
        set(3, 2, C("H7")); set(8, 2, C("H7"))
        set(2, 3, C("H7")); set(3, 3, C("A10")); set(4, 3, C("H7"))
        set(5, 3, C("H7")); set(6, 3, C("H7")); set(7, 3, C("H7"))
        set(8, 3, C("G19")); set(9, 3, C("H7"))
        set(2, 4, C("H7")); set(3, 4, C("A10")); set(4, 4, C("67"))
        set(5, 4, C("A6")); set(6, 4, C("67")); set(7, 4, C("G19"))
        set(8, 4, C("G19")); set(9, 4, C("H7"))
        set(1, 5, C("H7")); set(2, 5, C("G19")); set(3, 5, C("G19"))
        set(4, 5, C("G19")); set(5, 5, C("H12")); set(6, 5, C("G19"))
        set(7, 5, C("G19")); set(8, 5, C("G19")); set(9, 5, C("G19"))
        set(10, 5, C("H7"))
        set(1, 6, C("H7")); set(2, 6, C("G19")); set(3, 6, C("G19"))
        set(4, 6, C("DP")); set(5, 6, C("H18")); set(6, 6, C("H19"))
        set(7, 6, C("DP")); set(8, 6, C("G19")); set(9, 6, C("G19"))
        set(10, 6, C("H7"))
        set(1, 7, C("H7")); set(2, 7, C("67")); set(3, 7, C("E14"))
        set(4, 7, C("H7")); set(5, 7, C("H18")); set(6, 7, C("H19"))
        set(7, 7, C("H7")); set(8, 7, C("H12")); set(9, 7, C("M13"))
        set(10, 7, C("H7"))
        set(1, 8, C("H7")); set(2, 8, C("H2")); set(3, 8, C("H18"))
        set(4, 8, C("H18")); set(5, 8, C("H18")); set(6, 8, C("H18"))
        set(7, 8, C("H18")); set(8, 8, C("H18")); set(9, 8, C("H18"))
        set(10, 8, C("H7"))
        set(2, 9, C("H7")); set(3, 9, C("H7")); set(4, 9, C("H7"))
        set(5, 9, C("H7")); set(6, 9, C("H7")); set(7, 9, C("H7"))
        set(8, 9, C("H7")); set(9, 9, C("H7"))
    }

    private fun showBubble() {
        bubbleText?.let {
            try { petLayout?.removeView(it) } catch (_: Exception) {}
        }
        val w = words[kotlin.random.Random.nextInt(words.size)]
        bubbleText = TextView(this).apply {
            text = w
            setTextColor(Color.BLACK)
            background = GradientDrawable().also {
                it.setColor(Color.WHITE)
                it.setStroke(2, Color.parseColor("#333333"))
                it.cornerRadius = 24f
            }
            setPadding(24, 10, 24, 10)
            textSize = 13f
        }
        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
        )
        lp.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
        lp.topMargin = CANVAS_H + 6
        petLayout?.addView(bubbleText, lp)
        handler.postDelayed({
            bubbleText?.let { try { petLayout?.removeView(it) } catch (_: Exception) {} }
            bubbleText = null
        }, 3000)
    }

    private fun spawnHearts() {
        val emoji = arrayOf("❤️", "💕", "💗", "💖", "🩷", "♥")
        val n = 4 + (Math.random() * 5).toInt()
        repeat(n) {
            val h = TextView(this).apply {
                text = emoji[(Math.random() * emoji.size).toInt()]
                textSize = 14f + (Math.random() * 8).toFloat()
            }
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT
            )
            lp.gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            lp.topMargin = CANVAS_H + 6 + ((Math.random() - 0.5) * 40).toInt()
            lp.leftMargin = ((Math.random() - 0.5) * 80).toInt()
            petLayout?.addView(h, lp)
            handler.postDelayed({
                try { petLayout?.removeView(h) } catch (_: Exception) {}
            }, 1000)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        petLayout?.let { try { windowManager?.removeView(it) } catch (_: Exception) {} }
        petLayout = null
        super.onDestroy()
    }
}
