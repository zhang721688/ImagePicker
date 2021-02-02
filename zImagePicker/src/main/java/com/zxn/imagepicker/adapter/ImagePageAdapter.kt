package com.zxn.imagepicker.adapter

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.util.Utils
import uk.co.senab.photoview.PhotoView
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener
import java.util.*

class ImagePageAdapter(private val mActivity: Activity, images: ArrayList<ImageItem>) : PagerAdapter() {
    private val screenWidth: Int
    private val screenHeight: Int
//    private val imagePicker: ImagePicker
    private var images = ArrayList<ImageItem>()
    var listener: PhotoViewClickListener? = null
    fun setData(images: ArrayList<ImageItem>) {
        this.images = images
    }

    fun setPhotoViewClickListener(listener: PhotoViewClickListener?) {
        this.listener = listener
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val photoView = PhotoView(mActivity)
        val imageItem = images[position]
        ImagePicker.imageLoader?.displayImagePreview(mActivity, imageItem.path, photoView, screenWidth, screenHeight)
        photoView.onPhotoTapListener = OnPhotoTapListener { view, x, y -> if (listener != null) listener!!.OnPhotoTapListener(view, x, y) }
        container.addView(photoView)
        return photoView
    }

    override fun getCount(): Int {
        return images.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    interface PhotoViewClickListener {
        fun OnPhotoTapListener(view: View?, v: Float, v1: Float)
    }

    init {
        this.images = images
        val dm = Utils.getScreenPix(mActivity)
        screenWidth = dm.widthPixels
        screenHeight = dm.heightPixels
        //imagePicker = ImagePicker.instance
    }
}