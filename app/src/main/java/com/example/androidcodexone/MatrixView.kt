package com.example.androidcodexone

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
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

    private data class Column(
        var x: Float,
        var y: Float,
        var speed: Float,
        var length: Int
    )

    private val columns = mutableListOf<Column>()

    // ASCII printable characters combined with Katakana for a more authentic look
    private val chars: List<Char> =
        (33..126).map { it.toChar() } + (0x30A0..0x30FF).map { it.toChar() }

    // Speed multiplier to make columns fall 200% faster (3x overall
    // while update frequency is slowed down)
    private val speedMultiplier = 6f

    private var charHeight = 0f

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateColumns()
            invalidate()
            // Update characters half as often (every 100ms)
            postDelayed(this, 100)
        }
    }

    init {
        post(updateRunnable)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(updateRunnable)
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
            val length = Random.nextInt(5, 20)
            val y = Random.nextFloat() * h - length * charHeight
            val speed = (5 + Random.nextFloat() * 10) * speedMultiplier
            columns.add(Column(x, y, speed, length))
        }
    }

    private fun updateColumns() {
        val viewHeight = height.toFloat()
        for (column in columns) {
            column.y += column.speed
            if (column.y - column.length * charHeight > viewHeight) {
                column.length = Random.nextInt(5, 20)
                column.y = -column.length * charHeight
                column.speed = (5 + Random.nextFloat() * 10) * speedMultiplier
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (column in columns) {
            for (i in 0 until column.length) {
                val yPos = column.y - i * charHeight
                if (yPos < -charHeight || yPos > height + charHeight) continue
                paint.alpha = Random.nextInt(50, 256)
                val char = chars.random()
                canvas.drawText(char.toString(), column.x, yPos, paint)
            }
        }
    }
}
