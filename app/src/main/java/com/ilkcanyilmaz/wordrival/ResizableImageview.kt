package com.ilkcanyilmaz.wordrival

import android.content.Context
import android.util.AttributeSet


class ResizableImageview(context: Context?, attrs: AttributeSet?) :
    androidx.appcompat.widget.AppCompatImageView(context!!, attrs) {
    protected override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, width)
    }
}