package com.zxn.imagepicker.adapter

import android.Manifest
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.R
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.ui.ImageBaseActivity
import com.zxn.imagepicker.ui.ImageGridActivity
import com.zxn.imagepicker.util.Utils
import com.zxn.imagepicker.view.SuperCheckBox
import java.util.*

/**
 * 加载相册图片的RecyclerView适配器
 *
 * 用于替换原项目的GridView，使用局部刷新解决选中照片出现闪动问题
 * 替换为RecyclerView后只是不再会导致全局刷新，
 * 但还是会出现明显的重新加载图片，可能是picasso图片加载框架的问题
 *
 */
class ImageRecyclerAdapter(private val mActivity: Activity, images: ArrayList<ImageItem>?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
//    private val imagePicker: ImagePicker

    private var images //当前需要显示的所有的图片数据
            : ArrayList<ImageItem>? = null
    private val mSelectedImages //全局保存的已经选中的图片数据
            : ArrayList<ImageItem>

    private val isShowCamera //是否显示拍照按钮
            : Boolean
    private val mImageSize //每个条目的大小
            : Int
    private val mInflater: LayoutInflater
    private var listener //图片被点击的监听
            : OnImageItemClickListener? = null

    fun setOnImageItemClickListener(listener: OnImageItemClickListener?) {
        this.listener = listener
    }

    interface OnImageItemClickListener {
        fun onImageItemClick(view: View?, imageItem: ImageItem?, position: Int)
    }

    fun refreshData(images: ArrayList<ImageItem>?) {
        if (images == null || images.size == 0) this.images = ArrayList() else this.images = images
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_CAMERA) {
            CameraViewHolder(mInflater.inflate(R.layout.adapter_camera_item, parent, false))
        } else ImageViewHolder(mInflater.inflate(R.layout.adapter_image_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CameraViewHolder) {
            holder.bindCamera()
        } else if (holder is ImageViewHolder) {
            holder.bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (isShowCamera) if (position == 0) ITEM_TYPE_CAMERA else ITEM_TYPE_NORMAL else ITEM_TYPE_NORMAL
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemCount(): Int {
        return if (isShowCamera) images!!.size + 1 else images!!.size
    }

    fun getItem(position: Int): ImageItem? {
        return if (isShowCamera) {
            if (position == 0) null else images!![position - 1]
        } else {
            images!![position]
        }
    }

    private inner class ImageViewHolder internal constructor(var rootView: View) : RecyclerView.ViewHolder(rootView) {
        var ivThumb: ImageView
        var mask: View
        var checkView: View
        var cbCheck: SuperCheckBox
        fun bind(position: Int) {
            val imageItem = getItem(position)
            ivThumb.setOnClickListener { if (listener != null) listener!!.onImageItemClick(rootView, imageItem, position) }
            checkView.setOnClickListener {
                cbCheck.isChecked = !cbCheck.isChecked
                val selectLimit = ImagePicker.selectLimit
                if (cbCheck.isChecked && mSelectedImages.size >= selectLimit) {
                    Toast.makeText(mActivity.applicationContext, mActivity.getString(R.string.ip_select_limit, selectLimit), Toast.LENGTH_SHORT).show()
                    cbCheck.isChecked = false
                    mask.visibility = View.GONE
                } else {
                    ImagePicker.addSelectedImageItem(position, imageItem!!, cbCheck.isChecked)
                    mask.visibility = View.VISIBLE
                }
            }
            //根据是否多选，显示或隐藏checkbox
            if (ImagePicker.isMultiMode) {
                cbCheck.visibility = View.VISIBLE
                val checked = mSelectedImages.contains(imageItem)
                if (checked) {
                    mask.visibility = View.VISIBLE
                    cbCheck.isChecked = true
                    //展示选中的序号.
                    if (ImagePicker.isShowSelectIndex) {
                        cbCheck.text = (ImagePicker.imageIndexOf(imageItem!!) + 1).toString()
                    }
                } else {
                    mask.visibility = View.GONE
                    cbCheck.isChecked = false
                    cbCheck.text = ""
                }
            } else {
                cbCheck.visibility = View.GONE
            }
            ImagePicker.imageLoader?.displayImage(mActivity, imageItem!!.path, ivThumb, mImageSize, mImageSize) //显示图片
            if (ImagePicker.checkBoxResource != 0) {
                cbCheck.setBackgroundResource(ImagePicker.checkBoxResource)
            }
        }

        init {
            ivThumb = itemView.findViewById<View>(R.id.iv_thumb) as ImageView
            mask = itemView.findViewById(R.id.mask)
            checkView = itemView.findViewById(R.id.checkView)
            cbCheck = itemView.findViewById<View>(R.id.cb_check) as SuperCheckBox
            itemView.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize) //让图片是个正方形
        }
    }

    private inner class CameraViewHolder internal constructor(var mItemView: View) : RecyclerView.ViewHolder(mItemView) {
        fun bindCamera() {
            mItemView.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mImageSize) //让图片是个正方形
            mItemView.tag = null
            mItemView.setOnClickListener {
                if (!(mActivity as ImageBaseActivity).checkPermission(Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.CAMERA), ImageGridActivity.REQUEST_PERMISSION_CAMERA)
                } else {
                    ImagePicker.takePicture(mActivity, ImagePicker.REQUEST_CODE_TAKE)
                }
            }
        }
    }

    companion object {
        private const val ITEM_TYPE_CAMERA = 0 //第一个条目是相机
        private const val ITEM_TYPE_NORMAL = 1 //第一个条目不是相机
    }

    /**
     * 构造方法
     */
    init {
        if (images == null || images.size == 0) this.images = ArrayList() else this.images = images
        mImageSize = Utils.getImageItemWidth(mActivity)
        //imagePicker = ImagePicker.instance
        isShowCamera = ImagePicker.isShowCamera
        mSelectedImages = ImagePicker.selectedImages
        mInflater = LayoutInflater.from(mActivity)
    }
}