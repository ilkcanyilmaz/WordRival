package com.ilkcanyilmaz.wordrival.utils

import android.content.res.Resources

fun pxToDp(value: Float):Float{
    return value * Resources.getSystem().displayMetrics.density.toInt()
}