package com.zxn.imagepicker.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.R
import com.zxn.imagepicker.bean.ImageFolder
import com.zxn.imagepicker.util.Utils
import java.util.*

class ImageFolderAdapter(private val mActivity: Activity, folders: MutableList<ImageFolder>?) : BaseAdapter() {

//    private val imagePicker: ImagePicker

    private val mInflater: LayoutInflater
    private val mImageSize: Int
    private var imageFolders: MutableList<ImageFolder>? = null
    private var lastSelected = 0
    fun refreshData(folders: MutableList<ImageFolder>?) {
        if (folders != null && folders.size > 0) imageFolders = folders else imageFolders!!.clear()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return imageFolders!!.size
    }

    override fun getItem(position: Int): ImageFolder {
        return imageFolders!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.adapter_folder_list_item, parent, false)
            holder = ViewHolder(convertView)
        } else {
            holder = convertView.tag as ViewHolder
        }
        val folder = getItem(position)
        holder.folderName.text = folder.name
        holder.imageCount.text = mActivity.getString(R.string.ip_folder_image_count, folder.images.size)
        ImagePicker.imageLoader?.displayImage(mActivity, folder.cover.path, holder.cover, mImageSize, mImageSize)
        if (lastSelected == position) {
            holder.folderCheck.visibility = View.VISIBLE
        } else {
            holder.folderCheck.visibility = View.INVISIBLE
        }
        return convertView
    }

    var selectIndex: Int
        get() = lastSelected
        set(i) {
            if (lastSelected == i) {
                return
            }
            lastSelected = i
            notifyDataSetChanged()
        }

    private inner class ViewHolder(view: View) {
        var cover: ImageView
        var folderName: TextView
        var imageCount: TextView
        var folderCheck: ImageView

        init {
            cover = view.findViewById<View>(R.id.iv_cover) as ImageView
            folderName = view.findViewById<View>(R.id.tv_folder_name) as TextView
            imageCount = view.findViewById<View>(R.id.tv_image_count) as TextView
            folderCheck = view.findViewById<View>(R.id.iv_folder_check) as ImageView
            view.tag = this
        }
    }

    init {
        imageFolders = if (folders != null && folders.size > 0) folders else ArrayList()
//        imagePicker = ImagePicker.instance
        mImageSize = Utils.getImageItemWidth(mActivity)
        mInflater = mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}