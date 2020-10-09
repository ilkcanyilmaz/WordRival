package com.ilkcanyilmaz.wordrival.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide


@BindingAdapter("android:downloadUrl")
fun downloadImage(view: ImageView, url: String?) {
    Glide.with(view.context).load(url).into(view);
}
