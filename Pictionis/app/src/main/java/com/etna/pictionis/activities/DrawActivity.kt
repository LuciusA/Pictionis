package com.etna.pictionis.activities

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

typealias OnDrawListener = (List<Point>?) -> Unit

class SimpleDrawView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet), View.OnTouchListener {
    private val mLineHistoryList: MutableList<MutableList<Point>> = mutableListOf()
    private val mPaint: Paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = STROKE_WIDTH
            style = Paint.Style.STROKE
            color = Color.RED
        }
    }

    var drawListener: OnDrawListener? = null

    init {
        isFocusable = true
        isFocusableInTouchMode = true
        this.setOnTouchListener(this)
    }

    override fun onDraw(canvas: Canvas) {
        mLineHistoryList.forEach { line ->
            if (line.size == 1) {
                onDrawPoint(canvas, line)
            }
            else {
                onDrawLine(canvas, line)
            }
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onTouchStart(event)
            }
            MotionEvent.ACTION_MOVE -> {
                onTouchMove(event)
            }
            MotionEvent.ACTION_UP   -> {
                onTouchEnd()
            }
        }

        return true
    }

    fun clear() {
        mLineHistoryList.clear()
        invalidate()
    }

    fun drawLine(lineList: List<Point>) {
        mLineHistoryList.add(lineList.toMutableList())
        invalidate()
    }

    private fun onDrawPoint(canvas: Canvas, line: MutableList<Point>) {
        line.firstOrNull()
            ?.let { point -> canvas.drawPoint(point.x.toFloat(), point.y.toFloat(), mPaint) }
    }

    private fun onDrawLine(canvas: Canvas, line: List<Point>) {
        val path = Path()
        line.forEachIndexed { index, point ->
            val x = point.x.toFloat()
            val y = point.y.toFloat()
            if (index == 0) {
                path.moveTo(x, y)
            }
            else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, mPaint)
    }

    private fun onTouchStart(event: MotionEvent) {
        mLineHistoryList.add(mutableListOf())
        addPointToCurrentLineHistory(event.x, event.y)
        invalidate()
    }

    private fun onTouchMove(event: MotionEvent) {
        addPointToCurrentLineHistory(event.x, event.y)
        invalidate()
    }

    private fun onTouchEnd() {
        drawListener?.invoke(mLineHistoryList.lastOrNull())
    }

    private fun addPointToCurrentLineHistory(x: Float, y: Float) {
        Point().apply {
            this.x = x.toInt()
            this.y = y.toInt()
        }.let { point ->
            mLineHistoryList.last().add(point)
        }
    }

    companion object {
        private val STROKE_WIDTH = 4f
    }
}