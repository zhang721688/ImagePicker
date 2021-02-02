package com.zxn.imagepicker.loader

import android.app.Activity
import android.widget.ImageView
import java.io.Serializable

/**
 * 描    述：ImageLoader抽象类，外部需要实现这个类去加载图片， 尽力减少对第三方库的依赖，所以这么干了
 */
interface ImageLoader : Serializable {
    fun displayImage(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int)
    fun displayImagePreview(activity: Activity, path: String, imageView: ImageView, width: Int, height: Int)
    fun clearMemoryCache()
}