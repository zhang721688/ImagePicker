package com.zxn.imagepicker.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CompoundButton
import android.widget.Toast
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.ImagePicker.OnImageSelectedListener
import com.zxn.imagepicker.R
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.ui.ImagePreviewActivity
import com.zxn.imagepicker.util.NavigationBarChangeListener
import com.zxn.imagepicker.util.NavigationBarChangeListener.OnSoftInputStateChangeListener
import com.zxn.imagepicker.util.Utils
import com.zxn.imagepicker.view.SuperCheckBox

class ImagePreviewActivity : ImagePreviewBaseActivity(), OnImageSelectedListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private var isOrigin //是否选中原图
            = false
    private var mCbCheck //是否选中当前图片的CheckBox
            : SuperCheckBox? = null
    private var mCbOrigin //原图
            : SuperCheckBox? = null
    private var mBtnOk //确认图片的选择
            : Button? = null
    private lateinit var bottomBar: View
    private lateinit var marginView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isOrigin = intent.getBooleanExtra(ISORIGIN, false)
        imagePicker.addOnImageSelectedListener(this)
        mBtnOk = findViewById<View>(R.id.btn_ok) as Button
        mBtnOk!!.visibility = View.VISIBLE
        mBtnOk!!.setOnClickListener(this)
        bottomBar = findViewById(R.id.bottom_bar)
        bottomBar.setVisibility(View.VISIBLE)
        mCbCheck = findViewById<View>(R.id.cb_check) as SuperCheckBox
        mCbOrigin = findViewById<View>(R.id.cb_origin) as SuperCheckBox
        marginView = findViewById(R.id.margin_bottom)
        mCbOrigin!!.text = getString(R.string.ip_origin)
        mCbOrigin!!.setOnCheckedChangeListener(this)
        mCbOrigin!!.isChecked = isOrigin

        //初始化当前页面的状态
        onImageSelected(0, null, false)
        val item = mImageItems?.get(mCurrentPosition)
        val isSelected = item?.let { imagePicker.isSelect(it) }
        mTitleCount?.text = getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems?.size)
        mCbCheck!!.isChecked = isSelected
        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewPager?.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mCurrentPosition = position
                val item = mImageItems[mCurrentPosition]
                val isSelected = imagePicker.isSelect(item)
                mCbCheck!!.isChecked = isSelected
                mTitleCount?.text = getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems.size)
            }
        })
        //当点击当前选中按钮的时候，需要根据当前的选中状态添加和移除图片
        mCbCheck!!.setOnClickListener {
            val imageItem = mImageItems[mCurrentPosition]
            val selectLimit = imagePicker.selectLimit
            if (mCbCheck!!.isChecked && selectedImages.size >= selectLimit) {
                Toast.makeText(this@ImagePreviewActivity, getString(R.string.ip_select_limit, selectLimit), Toast.LENGTH_SHORT).show()
                mCbCheck!!.isChecked = false
            } else {
                imagePicker.addSelectedImageItem(mCurrentPosition, imageItem, mCbCheck!!.isChecked)
            }
        }
        NavigationBarChangeListener.with(this).setListener(object : OnSoftInputStateChangeListener {
            override fun onNavigationBarShow(orientation: Int, height: Int) {
                marginView.setVisibility(View.VISIBLE)
                val layoutParams = marginView.getLayoutParams()
                if (layoutParams.height == 0) {
                    layoutParams.height = Utils.getNavigationBarHeight(this@ImagePreviewActivity)
                    marginView.requestLayout()
                }
            }

            override fun onNavigationBarHide(orientation: Int) {
                marginView.setVisibility(View.GONE)
            }
        })
        NavigationBarChangeListener.with(this, NavigationBarChangeListener.ORIENTATION_HORIZONTAL)
                .setListener(object : OnSoftInputStateChangeListener {
                    override fun onNavigationBarShow(orientation: Int, height: Int) {
                        topBar.setPadding(0, 0, height, 0)
                        bottomBar.setPadding(0, 0, height, 0)
                    }

                    override fun onNavigationBarHide(orientation: Int) {
                        topBar.setPadding(0, 0, 0, 0)
                        bottomBar.setPadding(0, 0, 0, 0)
                    }
                })
    }

    /**
     * 图片添加成功后，修改当前图片的选中数量
     * 当调用 addSelectedImageItem 或 deleteSelectedImageItem 都会触发当前回调
     */
    override fun onImageSelected(position: Int, item: ImageItem?, isAdd: Boolean) {
        if (imagePicker.selectImageCount > 0) {
            mBtnOk!!.text = getString(R.string.ip_select_complete, imagePicker.selectImageCount, imagePicker.selectLimit)
        } else {
            mBtnOk!!.text = getString(R.string.ip_complete)
        }
        if (mCbOrigin!!.isChecked) {
            var size: Long = 0
            for (imageItem in selectedImages) size += imageItem.size
            val fileSize = Formatter.formatFileSize(this, size)
            mCbOrigin!!.text = getString(R.string.ip_origin_size, fileSize)
        }
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_ok) {
            if (imagePicker.selectedImages.size == 0) {
                mCbCheck!!.isChecked = true
                val imageItem = mImageItems[mCurrentPosition]
                imagePicker.addSelectedImageItem(mCurrentPosition, imageItem, mCbCheck!!.isChecked)
            }
            val intent = Intent()
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.selectedImages)
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent)
            finish()
        } else if (id == R.id.btn_back) {
            val intent = Intent()
            intent.putExtra(ISORIGIN, isOrigin)
            setResult(ImagePicker.RESULT_CODE_BACK, intent)
            finish()
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra(ISORIGIN, isOrigin)
        setResult(ImagePicker.RESULT_CODE_BACK, intent)
        finish()
        super.onBackPressed()
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        val id = buttonView.id
        if (id == R.id.cb_origin) {
            if (isChecked) {
                var size: Long = 0
                for (item in selectedImages) size += item.size
                val fileSize = Formatter.formatFileSize(this, size)
                isOrigin = true
                mCbOrigin!!.text = getString(R.string.ip_origin_size, fileSize)
            } else {
                isOrigin = false
                mCbOrigin!!.text = getString(R.string.ip_origin)
            }
        }
    }

    override fun onDestroy() {
        imagePicker.removeOnImageSelectedListener(this)
        super.onDestroy()
    }

    /**
     * 单击时，隐藏头和尾
     */
    override fun onImageSingleTap() {
        if (topBar.visibility == View.VISIBLE) {
            topBar.animation = AnimationUtils.loadAnimation(this, R.anim.top_out)
            bottomBar!!.animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
            topBar.visibility = View.GONE
            bottomBar!!.visibility = View.GONE
            tintManager!!.setStatusBarTintResource(Color.TRANSPARENT) //通知栏所需颜色
            //给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            topBar.animation = AnimationUtils.loadAnimation(this, R.anim.top_in)
            bottomBar!!.animation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
            topBar.visibility = View.VISIBLE
            bottomBar!!.visibility = View.VISIBLE
            tintManager!!.setStatusBarTintResource(R.color.ip_color_primary_dark) //通知栏所需颜色
            //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    companion object {
        const val ISORIGIN = "isOrigin"
    }
}