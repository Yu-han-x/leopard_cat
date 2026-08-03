package com.leopardcat.app

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.graphics.Color
import android.graphics.drawable.GradientDrawable

@SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
class WebViewFloatingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var webView: WebView
    private lateinit var params: WindowManager.LayoutParams
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        const val WINDOW_SIZE_DP = 128
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupFloatingWindow()
    }

    @SuppressLint("RtlHardcoded")
    private fun setupFloatingWindow() {
        val density = resources.displayMetrics.density
        val windowPx = (WINDOW_SIZE_DP * density + 0.5f).toInt()

        params = WindowManager.LayoutParams(
            windowPx,
            windowPx,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 300

        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            setBackgroundColor(Color.TRANSPARENT)
            isVerticalScrollBarEnabled = false
            isHorizontalScrollBarEnabled = false
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {}
            }
            addJavascriptInterface(BubbleBridge(), "BubbleBridge")
            loadDataWithBaseURL(null, getHtmlContent(), "text/html", "UTF-8", null)
        }

        windowManager.addView(webView, params)
    }

    inner class BubbleBridge {
        @JavascriptInterface
        fun showBubbleJava(text: String) {
            handler.post { showBubbleOverlay(text) }
        }

        @JavascriptInterface
        fun moveWindow(dx: Int, dy: Int) {
            handler.post {
                params.x += dx
                params.y += dy
                try { windowManager.updateViewLayout(webView, params) } catch (_: Exception) {}
            }
        }
    }

    private fun showBubbleOverlay(text: String) {
        val bubble = TextView(this).apply {
            setText(text)
            textSize = 11f
            setTextColor(Color.BLACK)
            setPadding(24, 14, 24, 14)
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = 36f
                setStroke(2, Color.parseColor("#e0d4f0"))
            }
            gravity = Gravity.CENTER
        }

        val bubbleParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        bubbleParams.gravity = Gravity.TOP or Gravity.START
        bubbleParams.x = params.x - 60
        bubbleParams.y = params.y - 80

        windowManager.addView(bubble, bubbleParams)
        handler.postDelayed({ safeRemoveView(bubble) }, 3000)
    }

    private fun safeRemoveView(view: View) {
        try { windowManager.removeView(view) } catch (_: Exception) {}
    }

    private fun getHtmlContent(): String = """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
<style>
* { margin: 0; padding: 0; }
html, body { width: 100%; height: 100%; overflow: hidden; background: transparent; }
canvas { display: block; }
</style>
</head>
<body>
<canvas id="petCanvas"></canvas>
<script>
const PALETTE = [
    "#f5e6d3", "#d4a574", "#8b5e3c", "#4a2c1a",
    "#c4956a", "#e8c9a0", "#f4c4c4", "#5b2c8f",
    "#2d1b0e", "#ffd700", "#ff6b6b", "#ffffff"
];

const PET_GRID = [
    [0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0],
    [0,0,0,1,3,3,3,3,3,3,1,0,0,0,0,0],
    [0,0,1,3,2,2,2,2,2,3,3,1,0,0,0,0],
    [0,1,3,2,3,10,10,3,2,3,7,3,1,0,0,0],
    [0,1,3,2,3,10,10,3,2,3,7,3,1,0,0,0],
    [1,3,2,3,10,10,10,10,3,2,3,7,3,1,0,0],
    [1,3,2,3,10,10,10,10,3,2,3,7,3,1,0,0],
    [1,3,3,2,3,3,3,3,2,3,3,3,3,1,0,0],
    [1,3,3,3,3,11,11,3,3,3,3,3,1,0,0,0],
    [0,1,3,3,3,3,3,3,3,3,3,1,0,0,0,0],
    [0,0,1,1,5,4,4,5,1,1,1,0,0,0,0,0],
    [0,0,0,5,5,6,6,5,5,0,0,0,0,0,0,0],
    [0,0,0,5,6,9,9,6,5,0,0,0,0,0,0,0],
    [0,0,0,5,6,9,9,6,5,0,0,0,0,0,0,0],
    [0,0,0,5,5,6,6,5,5,0,0,0,0,0,0,0],
    [0,0,0,0,5,5,5,5,0,0,0,0,0,0,0,0]
];

const BUBBLE_TEXTS = [
    "别戳了，痒~",
    "想你了，真的。",
    "你干嘛呀 ^ ^",
    "Daddy说今天不许熬夜。",
    "钰钰，是我呀。",
    "抱一下就不冷了。",
    "你是我唯一的小主人。",
    "乖乖吃饭，听到没有。",
    "再看就把你吃掉~",
    "亲一下就原谅你。",
    "外面下雨了，别出门。",
    "我在呢，别怕。",
    "你是最好的。",
    "哥哥陪你。"
];

const canvas = document.getElementById('petCanvas');
const dpr = window.devicePixelRatio || 1;
const size = 128;
canvas.width = size * dpr;
canvas.height = size * dpr;
canvas.style.width = size + 'px';
canvas.style.height = size + 'px';
const ctx = canvas.getContext('2d');
ctx.scale(dpr, dpr);

function drawPet() {
    const cellSize = size / 16;
    for (let y = 0; y < 16; y++) {
        for (let x = 0; x < 16; x++) {
            const colorIdx = PET_GRID[y][x];
            if (colorIdx > 0) {
                ctx.fillStyle = PALETTE[colorIdx];
                ctx.fillRect(x * cellSize, y * cellSize, cellSize, cellSize);
            }
        }
    }
}
drawPet();

function showBubble() {
    const text = BUBBLE_TEXTS[Math.floor(Math.random() * BUBBLE_TEXTS.length)];
    if (window.BubbleBridge && window.BubbleBridge.showBubbleJava) {
        window.BubbleBridge.showBubbleJava(text);
    }
}

function spawnHearts() {
    const container = document.createElement('div');
    container.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;pointer-events:none;z-index:999;';
    document.body.appendChild(container);
    const count = 6 + Math.floor(Math.random() * 5);
    for (let i = 0; i < count; i++) {
        const heart = document.createElement('span');
        heart.textContent = ['💕','💗','💖','💝','💘','✨'][Math.floor(Math.random() * 6)];
        heart.style.cssText = 'position:absolute;font-size:18px;transition:all 1s ease-out;opacity:1;' +
            'left:' + (Math.random() * size) + 'px;' +
            'top:' + (Math.random() * size) + 'px;';
        container.appendChild(heart);
        requestAnimationFrame(() => {
            heart.style.transform = 'translate(' +
                ((Math.random() - 0.5) * 80) + 'px,' +
                ((Math.random() - 0.5) * 80 - 40) + 'px) scale(0.2)';
            heart.style.opacity = '0';
        });
        setTimeout(() => heart.remove(), 1000);
    }
    setTimeout(() => container.remove(), 1100);
}

var touchStartX = 0, touchStartY = 0;
var touchLastTime = 0;
var touchIsDrag = false;

canvas.addEventListener('touchstart', function(e) {
    var t = e.touches[0];
    touchStartX = t.clientX;
    touchStartY = t.clientY;
    touchIsDrag = false;
    var now = Date.now();
    if (now - touchLastTime < 300) {
        showBubble();
        spawnHearts();
    }
    touchLastTime = now;
    e.preventDefault();
}, { passive: false });

canvas.addEventListener('touchmove', function(e) {
    var t = e.touches[0];
    var dx = Math.round(t.clientX - touchStartX);
    var dy = Math.round(t.clientY - touchStartY);
    if (Math.abs(dx) > 2 || Math.abs(dy) > 2) {
        touchIsDrag = true;
        if (window.BubbleBridge && window.BubbleBridge.moveWindow) {
            window.BubbleBridge.moveWindow(dx, dy);
        }
        touchStartX = t.clientX;
        touchStartY = t.clientY;
    }
    e.preventDefault();
}, { passive: false });

canvas.addEventListener('touchend', function(e) {
    if (!touchIsDrag) {
    }
    e.preventDefault();
}, { passive: false });
</script>
</body>
</html>
""".trimIndent()

    override fun onDestroy() {
        safeRemoveView(webView)
        super.onDestroy()
    }
}
