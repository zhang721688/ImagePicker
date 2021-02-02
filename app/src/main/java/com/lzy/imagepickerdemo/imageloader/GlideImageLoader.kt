package com.lzy.imagepickerdemo.imageloader

import android.app.Activity
import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.lzy.imagepickerdemo.R
import com.zxn.imagepicker.loader.ImageLoader
import java.io.File

/**
 * Created by zxn on 2019/5/17.
 */
class GlideImageLoader : ImageLoader {
    override fun displayImage(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        Glide.with(activity) //配置上下文
                .load(Uri.fromFile(File(path))) //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .apply(RequestOptions
                        .errorOf(R.drawable.ic_default_image) //设置错误图片
                        .placeholder(R.drawable.ic_default_image) //设置占位图片
                        .diskCacheStrategy(DiskCacheStrategy.ALL) //缓存全尺寸
                )
                .into(imageView)
    }

    override fun displayImagePreview(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int) {
        Glide.with(activity) //配置上下文
                .load(Uri.fromFile(File(path))) //设置图片路径(fix #8,文件名包含%符号 无法识别和显示)
                .apply(RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL)) //缓存全尺寸
                .into(imageView)
    }

    override fun clearMemoryCache() {}
}