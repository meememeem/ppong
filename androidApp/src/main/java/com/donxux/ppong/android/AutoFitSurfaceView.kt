package com.donxux.ppong.android

import android.content.Context
import android.util.AttributeSet
import android.view.SurfaceView
import kotlin.math.roundToInt

class AutoFitSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) :
    SurfaceView(context, attrs, defStyle) {
    private var ratioWidth = 0
    private var ratioHeight = 0

    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        ratioWidth = width
        ratioHeight = height
        holder.setFixedSize(height, width)
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        if (ratioWidth == 0 || ratioHeight == 0) {
            setMeasuredDimension(width, height)
        } else {
            val ratio = ratioWidth.toDouble() / ratioHeight.toDouble()

            if (width < height * ratio) {
                setMeasuredDimension((height * ratio).roundToInt(), height)
            } else {
                setMeasuredDimension(width, (width / ratio).roundToInt())
            }
        }
    }
}