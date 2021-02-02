package com.lzy.imagepickerdemo.imageloader

import android.app.Activity
import android.net.Uri
import android.widget.ImageView
import com.nostra13.universalimageloader.core.assist.ImageSize
import com.zxn.imagepicker.loader.ImageLoader
import java.io.File

class UILImageLoader : ImageLoader {
    override fun displayImage(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        val size = ImageSize(width, height)
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(Uri.fromFile(File(path)).toString(), imageView, size)
    }

    override fun displayImagePreview(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        val size = ImageSize(width, height)
        com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(Uri.fromFile(File(path)).toString(), imageView, size)
    }

    override fun clearMemoryCache() {}
}