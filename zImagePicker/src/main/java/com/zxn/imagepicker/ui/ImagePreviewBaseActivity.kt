package com.zxn.imagepicker.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.zxn.imagepicker.DataHolder
import com.zxn.imagepicker.DataHolder.retrieve
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.R
import com.zxn.imagepicker.adapter.ImagePageAdapter
import com.zxn.imagepicker.adapter.ImagePageAdapter.PhotoViewClickListener
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.util.Utils
import com.zxn.imagepicker.view.ViewPagerFixed
import java.util.*

/**
 * ：
 * 修订历史：图片预览的基类
 */
abstract class ImagePreviewBaseActivity : ImageBaseActivity() {
    protected var imagePicker: ImagePicker = ImagePicker/*.instance*/
    //跳转进ImagePreviewFragment的图片文件夹

    @JvmField
    protected var mImageItems
            : ArrayList<ImageItem> = ArrayList()

    @JvmField
    protected var mCurrentPosition = 0 //跳转进ImagePreviewFragment时的序号，第几个图片

    @JvmField
    protected var mTitleCount //显示当前图片的位置  例如  5/31
            : TextView? = null
    protected var selectedImages //所有已经选中的图片
            : ArrayList<ImageItem> = ArrayList()

    protected var content: View? = null

    //@JvmField
    protected lateinit var topBar: View

    @JvmField
    protected var mViewPager: ViewPagerFixed? = null

    @JvmField
    protected var mAdapter: ImagePageAdapter? = null
    protected var isFromItems = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)
        mCurrentPosition = intent.getIntExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0)
        isFromItems = intent.getBooleanExtra(ImagePicker.EXTRA_FROM_ITEMS, false)
        mImageItems = if (isFromItems) {
            // 据说这样会导致大量图片崩溃
            intent.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS) as ArrayList<ImageItem>
        } else {
            // 下面采用弱引用会导致预览崩溃
            retrieve(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS) as ArrayList<ImageItem>
        }
//        imagePicker = ImagePicker.instance
        selectedImages = imagePicker.selectedImages

        //初始化控件
        content = findViewById(R.id.content)

        //因为状态栏透明后，布局整体会上移，所以给头部加上状态栏的margin值，保证头部不会被覆盖
        topBar = findViewById(R.id.top_bar)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val params = topBar.getLayoutParams() as RelativeLayout.LayoutParams
            params.topMargin = Utils.getStatusHeight(this)
            topBar.setLayoutParams(params)
        }
        topBar.findViewById<View>(R.id.btn_ok).visibility = View.GONE
        topBar.findViewById<View>(R.id.btn_back).setOnClickListener { finish() }
        mTitleCount = findViewById<View>(R.id.tv_des) as TextView
        mViewPager = findViewById<View>(R.id.viewpager) as ViewPagerFixed
        mAdapter = ImagePageAdapter(this, mImageItems!!)
        mAdapter!!.setPhotoViewClickListener(object : PhotoViewClickListener {
            override fun OnPhotoTapListener(view: View?, v: Float, v1: Float) {
                onImageSingleTap()
            }
        })
        mViewPager!!.adapter = mAdapter
        mViewPager!!.setCurrentItem(mCurrentPosition, false)

        //初始化当前页面的状态
        mTitleCount!!.text = getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems!!.size)
    }

    /**
     * 单击时，隐藏头和尾
     */
    abstract fun onImageSingleTap()
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        ImagePicker.restoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ImagePicker.saveInstanceState(outState)
    }
}