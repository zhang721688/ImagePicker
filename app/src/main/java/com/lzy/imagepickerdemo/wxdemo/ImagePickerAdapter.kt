package com.lzy.imagepickerdemo.wxdemo

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.lzy.imagepickerdemo.R
import com.lzy.imagepickerdemo.wxdemo.ImagePickerAdapter.SelectedPicViewHolder
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.bean.ImageItem
import java.util.*

/**
 * 微信图片选择的Adapter
 */
class ImagePickerAdapter(private val mContext: Context, data: List<ImageItem>?, private val maxImgCount: Int) : RecyclerView.Adapter<SelectedPicViewHolder>() {
    private var mData: MutableList<ImageItem>? = null
    private val mInflater: LayoutInflater
    private var listener: OnRecyclerViewItemClickListener? = null
    private var isAdded //是否额外添加了最后一个图片
            = false

    interface OnRecyclerViewItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    fun setOnItemClickListener(listener: OnRecyclerViewItemClickListener?) {
        this.listener = listener
    }

    //由于图片未选满时，最后一张显示添加图片，因此这个方法返回真正的已选图片
    var images: List<ImageItem>?
        get() =//由于图片未选满时，最后一张显示添加图片，因此这个方法返回真正的已选图片
            if (isAdded) ArrayList(mData!!.subList(0, mData!!.size - 1)) else mData
        set(data) {
            mData = ArrayList(data)
            isAdded = if (itemCount < maxImgCount) {
                mData?.add(ImageItem())
                true
            } else {
                false
            }
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedPicViewHolder {
        return SelectedPicViewHolder(mInflater.inflate(R.layout.list_item_image, parent, false))
    }

    override fun onBindViewHolder(holder: SelectedPicViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return mData!!.size
    }

    inner class SelectedPicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val iv_img: ImageView
        private var clickPosition = 0
        fun bind(position: Int) {
            //设置条目的点击事件
            itemView.setOnClickListener(this)
            //根据条目位置设置图片
            val item = mData!![position]
            clickPosition = if (isAdded && position == itemCount - 1) {
                iv_img.setImageResource(R.drawable.selector_image_add)
                WxDemoActivity.IMAGE_ITEM_ADD
            } else {
                ImagePicker.imageLoader?.displayImage(mContext as Activity, item.path, iv_img, 0, 0)
                position
            }
        }

        override fun onClick(v: View) {
            if (listener != null) listener?.onItemClick(v, clickPosition)
        }

        init {
            iv_img = itemView.findViewById<View>(R.id.iv_img) as ImageView
        }
    }

    init {
        mInflater = LayoutInflater.from(mContext)
        images = data
    }
}