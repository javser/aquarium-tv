package ru.javser.aquarium

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import java.util.Random
import kotlin.math.sin
import kotlin.math.sqrt

class AquariumView(context: Context) : View(context) {

    private val random = Random()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var background: Bitmap? = null
    private val fishes = mutableListOf<Fish>()
    private val bubbles = mutableListOf<Bubble>()

    private var running = false
    private var lastTimeMs = 0L
    private var nextBubbleMs = 0L
    private var startMs = 0L

    private class Fish(
        val bitmap: Bitmap,
        var x: Float, var y: Float,
        var targetX: Float, var targetY: Float,
        var speed: Float, var scale: Float,
        var facingRight: Boolean, val phase: Float
    )

    private class Bubble(
        var x: Float, var y: Float, val r: Float,
        val speed: Float, val phase: Float
    )

    fun start() {
        if (running) return
        running = true
        loadAssets()
        startMs = System.currentTimeMillis()
        lastTimeMs = startMs
        postInvalidateOnAnimation()
    }

    fun stop() { running = false }

    private fun loadAssets() {
        val am = context.assets
        background = runCatching {
            am.open("background.png").use { BitmapFactory.decodeStream(it) }
        }.getOrNull()

        fishes.clear()
        val names = (am.list("fish") ?: emptyArray()).filter { it.endsWith(".png", true) }
        for (name in names) {
            val raw = runCatching {
                am.open("fish/$name").use { BitmapFactory.decodeStream(it) }
            }.getOrNull() ?: continue

            // не грузим огромные фото в память слабого ТВ
            val bmp = if (raw.width > 1200) {
                val k = 1200f / raw.width
                Bitmap.createScaledBitmap(raw, (raw.width * k).toInt(), (raw.height * k).toInt(), true)
            } else raw

            fishes.add(
                Fish(
                    bitmap = WhiteBackgroundRemover.removeWhite(bmp),
                    x = random.nextFloat(), y = 0.2f + random.nextFloat() * 0.5f,
                    targetX = random.nextFloat(), targetY = 0.2f + random.nextFloat() * 0.5f,
                    speed = 0.05f + random.nextFloat() * 0.06f,
                    scale = 0.22f + random.nextFloat() * 0.12f,
                    facingRight = random.nextBoolean(),
                    phase = random.nextFloat() * 100f
                )
            )
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat(); val h = height.toFloat()
        if (w == 0f || h == 0f) return

        val now = System.currentTimeMillis()
        val dt = ((now - lastTimeMs) / 1000f).coerceAtMost(0.1f)
        lastTimeMs = now
        val t = (now - startMs) / 1000f

        background?.let { canvas.drawBitmap(it, null, RectF(0f, 0f, w, h), paint) }
            ?: canvas.drawColor(Color.parseColor("#7EC8E3"))

        if (running) { updateFish(); updateBubbles(dt, now) }
        drawBubbles(canvas, w, h, t)
        drawFish(canvas, w, h, t)

        // защита от выгорания: через 10 минут приглушаем экран
        if (now - startMs > 10 * 60 * 1000L) {
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(90, 0, 0, 0)
            canvas.drawRect(0f, 0f, w, h, paint)
        }

        if (running) postInvalidateOnAnimation()
    }

    private fun updateFish() {
        for (f in fishes) {
            val dx = f.targetX - f.x
            val dy = f.targetY - f.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < 0.02f) {
                f.targetX = 0.1f + random.nextFloat() * 0.8f
                f.targetY = 0.15f + random.nextFloat() * 0.55f
            } else {
                f.x += dx / dist * f.speed * 0.016f
                f.y += dy / dist * f.speed * 0.016f * 0.6f
                f.facingRight = dx > 0
            }
        }
    }

    private fun drawFish(canvas: Canvas, w: Float, h: Float, t: Float) {
        for (f in fishes) {
            val bh = f.scale * h
            val bw = bh * f.bitmap.width / f.bitmap.height
            val cx = f.x * w
            val cy = f.y * h + (sin(t * 2.0 + f.phase) * h * 0.01).toFloat()
            val rect = RectF(cx - bw / 2, cy - bh / 2, cx + bw / 2, cy + bh / 2)

            canvas.save()
            canvas.rotate((sin(t * 3.0 + f.phase) * 4).toFloat(), cx, cy)
            if (f.facingRight) {          // рисунок "смотрит" влево по умолчанию
                canvas.translate(cx, cy); canvas.scale(-1f, 1f); canvas.translate(-cx, -cy)
            }
            canvas.drawBitmap(f.bitmap, null, rect, paint)
            canvas.restore()
        }
    }

    private fun updateBubbles(dt: Float, now: Long) {
        if (now >= nextBubbleMs) {
            nextBubbleMs = now + 400 + (random.nextFloat() * 800).toLong()
            bubbles.add(Bubble(random.nextFloat(), 1.05f,
                0.006f + random.nextFloat() * 0.012f,
                0.08f + random.nextFloat() * 0.08f,
                random.nextFloat() * 100f))
        }
        val it = bubbles.iterator()
        while (it.hasNext()) {
            val b = it.next()
            b.y -= b.speed * dt
            if (b.y < -0.05f) it.remove()
        }
    }

    private fun drawBubbles(canvas: Canvas, w: Float, h: Float, t: Float) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = h * 0.004f
        paint.color = Color.argb(200, 255, 255, 255)
        for (b in bubbles) {
            val cx = (b.x + (sin(t * 1.5 + b.phase) * 0.008).toFloat()) * w
            canvas.drawCircle(cx, b.y * h, b.r * h, paint)
        }
        paint.style = Paint.Style.FILL
    }
}
