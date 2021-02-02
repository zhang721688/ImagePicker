package com.lzy.imagepickerdemo.imageloader

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.widget.ImageView
import com.lzy.imagepickerdemo.R
import com.zxn.imagepicker.loader.ImageLoader
import org.xutils.image.ImageOptions
import org.xutils.x
import java.io.File

class XUtils3ImageLoader : ImageLoader {
    override fun displayImage(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        val options = ImageOptions.Builder() //
                .setLoadingDrawableId(R.drawable.ic_default_image) //
                .setFailureDrawableId(R.drawable.ic_default_image) //
                .setConfig(Bitmap.Config.RGB_565) //
                .setSize(width, height) //
                .setCrop(false) //
                .setUseMemCache(true) //
                .build()
        x.image().bind(imageView, Uri.fromFile(File(path)).toString(), options)
    }

    override fun displayImagePreview(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        val options = ImageOptions.Builder() //
                .setConfig(Bitmap.Config.RGB_565) //
                .setSize(width, height) //
                .setCrop(false) //
                .setUseMemCache(true) //
                .build()
        x.image().bind(imageView, Uri.fromFile(File(path)).toString(), options)
    }

    override fun clearMemoryCache() {}
}