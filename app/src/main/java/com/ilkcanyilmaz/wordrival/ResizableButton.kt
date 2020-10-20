package com.ilkcanyilmaz.wordrival

import android.content.Context
import android.util.AttributeSet


class ResizableButton(context: Context?, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatImageButton(context!!, attrs) {
    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, width)
    }
}