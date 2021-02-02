package com.lzy.imagepickerdemo.imageloader

import android.app.Activity
import android.net.Uri
import android.widget.ImageView
import com.lzy.imagepickerdemo.R
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.zxn.imagepicker.loader.ImageLoader
import java.io.File

class PicassoImageLoader : ImageLoader {
    override fun displayImage(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        Picasso.with(activity) //
                .load(Uri.fromFile(File(path))) //
                .placeholder(R.drawable.ic_default_image) //
                .error(R.drawable.ic_default_image) //
                .resize(width, height) //
                .centerInside() //
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //
                .into(imageView)
    }

    override fun displayImagePreview(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        Picasso.with(activity) //
                .load(Uri.fromFile(File(path))) //
                .resize(width, height) //
                .centerInside() //
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE) //
                .into(imageView)
    }

    override fun clearMemoryCache() {}
}