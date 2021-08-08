package com.ilkcanyilmaz.wordrival.utils

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import com.ilkcanyilmaz.wordrival.R

class LevelView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        private const val DEFAULT_ENABLE_COLOR = Color.GREEN
        private const val DEFAULT_DISABLE_COLOR = Color.BLACK
        private const val DEFAULT_BORDER_WIDTH = 1.0f

        const val LEVEL1 = 1L
        const val LEVEL2 = 2L
        const val LEVEL3 = 3L
        const val LEVEL4 = 4L
        const val LEVEL5 = 5L
    }

    private var enableColor = DEFAULT_ENABLE_COLOR
    private var disableColor = DEFAULT_DISABLE_COLOR
    private var borderWidth = DEFAULT_BORDER_WIDTH

    private val paint = Paint()
    private val mouthPath = Path()
    private var size = 0

    var levelState = LEVEL1
        set(state) {
            field = state
            invalidate()
        }

    init {
        paint.isAntiAlias = true
        setupAttributes(attrs)
    }

    private fun setupAttributes(attrs: AttributeSet?) {
        // Obtain a typed array of attributes
        val typedArray = context.theme.obtainStyledAttributes(
            attrs, R.styleable.LevelView,
            0, 0
        )

        // Extract custom attributes into member variables
        levelState =
            typedArray.getInt(R.styleable.LevelView_levelState, LEVEL1.toInt()).toLong()
        enableColor =
            typedArray.getColor(R.styleable.LevelView_enableLineColor, DEFAULT_ENABLE_COLOR)

        borderWidth = typedArray.getDimension(
            R.styleable.LevelView_borderWidth,
            DEFAULT_BORDER_WIDTH
        )

        // TypedArray objects are shared and must be recycled.
        typedArray.recycle()
    }

    override fun onDraw(canvas: Canvas) {
        // call the super method to keep any drawing from the parent side.
        super.onDraw(canvas)
        drawText(canvas)

        when (levelState) {
            LEVEL1 -> {
                drawlevelLine(canvas, 0.28f, 0.18f,true)
                drawlevelLine(canvas, 0.42f, 0.32f,false)
                drawlevelLine(canvas, 0.56f, 0.46f,false)
                drawlevelLine(canvas, 0.70f, 0.60f,false)
                drawlevelLine(canvas, 0.84f, 0.74f,false)

            }
            LEVEL2 -> {
                drawlevelLine(canvas, 0.28f, 0.18f,true)
                drawlevelLine(canvas, 0.42f, 0.32f,true)
                drawlevelLine(canvas, 0.56f, 0.46f,false)
                drawlevelLine(canvas, 0.70f, 0.60f,false)
                drawlevelLine(canvas, 0.84f, 0.74f,false)
            }
            LEVEL3 -> {
                drawlevelLine(canvas, 0.28f, 0.18f,true)
                drawlevelLine(canvas, 0.42f, 0.32f,true)
                drawlevelLine(canvas, 0.56f, 0.46f,true)
                drawlevelLine(canvas, 0.70f, 0.60f,false)
                drawlevelLine(canvas, 0.84f, 0.74f,false)
            }
            LEVEL4 -> {
                drawlevelLine(canvas, 0.28f, 0.18f,true)
                drawlevelLine(canvas, 0.42f, 0.32f,true)
                drawlevelLine(canvas, 0.56f, 0.46f,true)
                drawlevelLine(canvas, 0.70f, 0.60f,true)
                drawlevelLine(canvas, 0.84f, 0.74f,false)
            }
        }
        //drawEyes(canvas)
        //drawMouth(canvas)
    }

    private fun drawlevelLine(canvas: Canvas, left: Float, right: Float, lineEnable:Boolean) {
        val corners = floatArrayOf(
            40f, 40f,   // Top left radius in px
            40f, 40f,   // Top right radius in px
            40f, 40f,     // Bottom right radius in px
            40f, 40f      // Bottom left radius in px
        )
        if(lineEnable){
            paint.color = enableColor

        }else{
            paint.color = disableColor
        }
        paint.style = Paint.Style.FILL
        val path = Path()
        path.addRoundRect(
            RectF(size * left, size * 0.6f, size * right, size * 0.2f),
            corners,
            Path.Direction.CW
        )
        canvas.drawPath(path, paint)
    }

    private fun drawText(canvas: Canvas) {

        paint.color = Color.BLACK
        paint.textSize = 32f
        paint.textAlign = Paint.Align.CENTER;
        val xPos = canvas.width / 2f
        val yPos = (canvas.height / 2 - (paint.descent() + paint.ascent()) / 2)

        canvas.drawText("LV.1", xPos, size * 0.86f, paint)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        size = Math.min(measuredWidth, measuredHeight)

        setMeasuredDimension(size, size)
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()

        bundle.putLong("happinessState", levelState)
        bundle.putParcelable("superState", super.onSaveInstanceState())

        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        var viewState = state

        if (viewState is Bundle) {
            levelState = viewState.getLong("happinessState", LEVEL1)
            viewState = viewState.getParcelable("superState")!!
        }

        super.onRestoreInstanceState(viewState)
    }
}