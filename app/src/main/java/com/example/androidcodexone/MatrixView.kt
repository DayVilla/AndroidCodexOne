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
        color = Color.GREEN
        textSize = 32f * resources.displayMetrics.density
        typeface = Typeface.MONOSPACE
    }

    private data class Symbol(var x: Float, var y: Float, var speed: Float)

    private val symbols = mutableListOf<Symbol>()
    private val chars = (33..126).map { it.toChar() } // printable ascii
    private var charHeight = 0f

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateSymbols()
            invalidate()
            postDelayed(this, 50)
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
        symbols.clear()
        charHeight = paint.fontSpacing
        val columns = (w / paint.measureText("0")).toInt()
        for (i in 0 until columns) {
            val x = i * paint.measureText("0")
            val y = Random.nextFloat() * h
            val speed = 5 + Random.nextFloat() * 10
            symbols.add(Symbol(x, y, speed))
        }
    }

    private fun updateSymbols() {
        val height = height.toFloat()
        for (symbol in symbols) {
            symbol.y += symbol.speed
            if (symbol.y > height) {
                symbol.y = -charHeight
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (symbol in symbols) {
            paint.alpha = Random.nextInt(50, 256)
            val char = chars.random()
            canvas.drawText(char.toString(), symbol.x, symbol.y, paint)
        }
    }
}
