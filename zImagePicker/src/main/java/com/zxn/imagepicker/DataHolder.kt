package com.zxn.imagepicker

import com.zxn.imagepicker.bean.ImageItem

/**
 * 新的DataHolder单例，使用单例和弱引用解决崩溃问题
 */
object DataHolder {

    const val DH_CURRENT_IMAGE_FOLDER_ITEMS = "dh_current_image_folder_items"

    private val data: MutableMap<String, List<ImageItem>> = mutableMapOf()

    fun save(id: String, `object`: List<ImageItem>) {
        data[id] = `object`
    }

    fun retrieve(id: String): Any {
        return data[id]!!
    }

}