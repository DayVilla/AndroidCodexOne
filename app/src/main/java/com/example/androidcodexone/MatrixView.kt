package com.example.androidcodexone

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.graphics.Typeface
import android.os.SystemClock
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import kotlin.random.Random

class MatrixView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        // Typical green color used for the Matrix effect
        color = Color.rgb(0, 255, 70)
        // Scale text to 30% of the previous size (70% smaller)
        textSize = 32f * resources.displayMetrics.density * 0.3f
        typeface = Typeface.MONOSPACE
    }

    private val emojiBitmap: Bitmap =
        BitmapFactory.decodeResource(resources, R.drawable.emojia)
    private val emojiRect = RectF()

    private data class Column(
        var x: Float,
        var y: Float,
        var speed: Float, // pixels per second
        var length: Int,
        var chars: CharArray
    )

    private val columns = mutableListOf<Column>()

    // ASCII printable characters combined with Katakana for a more authentic look
    private val chars: List<Char> =
        (33..126).map { it.toChar() } + (0x30A0..0x30FF).map { it.toChar() }

    /** Length range for each falling line */
    private val minLineLength = 10
    private val maxLineLength = 40

    private var charHeight = 0f

    private var lastFrameTime = 0L
    private var lastCharUpdate = 0L

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            val timeMs = frameTimeNanos / 1_000_000
            if (lastFrameTime == 0L) {
                lastFrameTime = timeMs
                lastCharUpdate = timeMs
            }
            val delta = timeMs - lastFrameTime
            updateColumns(delta)
            invalidate()
            lastFrameTime = timeMs
            Choreographer.getInstance().postFrameCallback(this)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        lastFrameTime = SystemClock.uptimeMillis()
        lastCharUpdate = lastFrameTime
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    override fun onDetachedFromWindow() {
        Choreographer.getInstance().removeFrameCallback(frameCallback)
        super.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        columns.clear()
        charHeight = paint.fontSpacing
        val columnWidth = paint.measureText("0")
        val count = (w / columnWidth).toInt()
        for (i in 0 until count) {
            val x = i * columnWidth + Random.nextFloat() * columnWidth * 0.3f
            val length = Random.nextInt(minLineLength, maxLineLength)
            val y = Random.nextFloat() * h - length * charHeight
            //val speed = Random.nextFloat(300f, 900f)
            val speed = Random.nextFloat() * (900f - 300f) + 300f
            val charsArr = CharArray(length) { chars.random() }
            columns.add(Column(x, y, speed, length, charsArr))
        }

        val centerX = w / 2f
        val centerY = h / 2f

        val maxDimPx = 200f * resources.displayMetrics.density
        val scale = kotlin.math.min(1f, kotlin.math.min(
            maxDimPx / emojiBitmap.width.toFloat(),
            maxDimPx / emojiBitmap.height.toFloat()
        ))
        val drawW = emojiBitmap.width * scale
        val drawH = emojiBitmap.height * scale
        val halfW = drawW / 2f
        val halfH = drawH / 2f
        emojiRect.set(centerX - halfW, centerY - halfH, centerX + halfW, centerY + halfH)
    }

    private fun updateColumns(deltaMs: Long) {
        val viewHeight = height.toFloat()
        val updateChars = SystemClock.uptimeMillis() - lastCharUpdate >= 100
        for (column in columns) {
            column.y += column.speed * deltaMs / 1000f
            if (column.y - column.length * charHeight > viewHeight) {
                column.length = Random.nextInt(minLineLength, maxLineLength)
                column.chars = CharArray(column.length) { chars.random() }
                column.y = -column.length * charHeight
                column.speed = Random.nextFloat() * (900f - 300f) + 300f
            }
            if (updateChars) {
                for (i in column.chars.indices) {
                    column.chars[i] = chars.random()
                }
            }
        }
        if (updateChars) {
            lastCharUpdate = SystemClock.uptimeMillis()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = emojiRect.centerX()
        val repelRadius = emojiRect.width() / 2f * 1.2f
        for (column in columns) {
            for (i in 0 until column.length) {
                val yPos = column.y - i * charHeight
                if (yPos < -charHeight || yPos > height + charHeight) continue
                paint.alpha = Random.nextInt(50, 256)
                val char = column.chars[i]

                var x = column.x
                if (yPos >= emojiRect.top && yPos <= emojiRect.bottom) {
                    val dx = column.x - centerX
                    val dist = kotlin.math.abs(dx)
                    if (dist < repelRadius) {
                        val direction = if (dx >= 0) 1f else -1f
                        val t = 1f - dist / repelRadius
                        x += direction * repelRadius * t
                    }
                }
                canvas.drawText(char.toString(), x, yPos, paint)
            }
        }

        canvas.drawBitmap(emojiBitmap, null, emojiRect, null)
    }
}
