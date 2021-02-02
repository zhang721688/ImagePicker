package com.zxn.imagepicker.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.R
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.util.BitmapUtil
import com.zxn.imagepicker.view.CropImageView
import com.zxn.imagepicker.view.CropImageView.OnBitmapSaveCompleteListener
import java.io.File
import java.util.*

class ImageCropActivity : ImageBaseActivity(), View.OnClickListener, OnBitmapSaveCompleteListener {

    private var mCropImageView: CropImageView? = null
    private var mBitmap: Bitmap? = null
    private var mIsSaveRectangle = false
    private var mOutputX = 0
    private var mOutputY = 0
    private var mImageItems: ArrayList<ImageItem> = ArrayList<ImageItem>()
    private var imagePicker: ImagePicker = ImagePicker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)

        //初始化View
        findViewById<View>(R.id.btn_back).setOnClickListener(this)
        val btn_ok = findViewById<View>(R.id.btn_ok) as Button
        btn_ok.text = getString(R.string.ip_complete)
        btn_ok.setOnClickListener(this)
        val tv_des = findViewById<View>(R.id.tv_des) as TextView
        tv_des.text = getString(R.string.ip_photo_crop)
        mCropImageView = findViewById<View>(R.id.cv_crop_image) as CropImageView
        mCropImageView!!.setOnBitmapSaveCompleteListener(this)

        //获取需要的参数
        mOutputX = imagePicker.outPutX
        mOutputY = imagePicker.outPutY
        mIsSaveRectangle = imagePicker.isSaveRectangle
        mImageItems = imagePicker.selectedImages
        val imagePath = mImageItems[0].path
        mCropImageView!!.focusStyle = imagePicker.style
        mCropImageView!!.focusWidth = imagePicker.focusWidth
        mCropImageView!!.focusHeight = imagePicker.focusHeight

        //缩放图片
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(imagePath, options)
        val displayMetrics = resources.displayMetrics
        options.inSampleSize = calculateInSampleSize(options, displayMetrics.widthPixels, displayMetrics.heightPixels)
        options.inJustDecodeBounds = false
        mBitmap = BitmapFactory.decodeFile(imagePath, options)
        //        mCropImageView.setImageBitmap(mBitmap);
        mBitmap?.let {
            //设置默认旋转角度
            mCropImageView!!.setImageBitmap(mCropImageView!!.rotate(it, BitmapUtil.getBitmapDegree(imagePath)))
        }
//        mCropImageView.setImageURI(Uri.fromFile(new File(imagePath)));
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            inSampleSize = if (width > height) {
                width / reqWidth
            } else {
                height / reqHeight
            }
        }
        return inSampleSize
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_back) {
            setResult(RESULT_CANCELED)
            finish()
        } else if (id == R.id.btn_ok) {
            mCropImageView!!.saveBitmapToFile(imagePicker.getCropCacheFolder(this), mOutputX, mOutputY, mIsSaveRectangle)
        }
    }

    override fun onBitmapSaveSuccess(file: File?) {
        //裁剪后替换掉返回数据的内容，但是不要改变全局中的选中数据
        mImageItems.removeAt(0)
        val imageItem = ImageItem()
        imageItem.path = file?.absolutePath
        mImageItems.add(imageItem)
        val intent = Intent()
        intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, mImageItems)
        setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //单选不需要裁剪，返回数据
        finish()
    }

    override fun onBitmapSaveError(file: File?) {

    }

    override fun onDestroy() {
        super.onDestroy()
        mCropImageView!!.setOnBitmapSaveCompleteListener(null)
        if (null != mBitmap && !mBitmap!!.isRecycled) {
            mBitmap!!.recycle()
            mBitmap = null
        }
    }
}