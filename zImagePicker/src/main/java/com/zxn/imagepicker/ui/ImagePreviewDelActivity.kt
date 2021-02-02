package com.zxn.imagepicker.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.R
import com.zxn.imagepicker.util.NavigationBarChangeListener
import com.zxn.imagepicker.util.NavigationBarChangeListener.OnSoftInputStateChangeListener

/**
 * 修订历史：预览已经选择的图片，并可以删除, 感谢 ikkong 的提交
 */
class ImagePreviewDelActivity : ImagePreviewBaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mBtnDel = findViewById<View>(R.id.btn_del) as ImageView
        mBtnDel.setOnClickListener(this)
        mBtnDel.visibility = View.VISIBLE
        topBar.findViewById<View>(R.id.btn_back).setOnClickListener(this)
        mTitleCount!!.text = getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems!!.size)
        //滑动ViewPager的时候，根据外界的数据改变当前的选中状态和当前的图片的位置描述文本
        mViewPager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                mCurrentPosition = position
                mTitleCount!!.text = getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems!!.size)
            }
        })
        NavigationBarChangeListener.with(this, NavigationBarChangeListener.ORIENTATION_HORIZONTAL)
                .setListener(object : OnSoftInputStateChangeListener {
                    override fun onNavigationBarShow(orientation: Int, height: Int) {
                        topBar.setPadding(0, 0, height, 0)
                    }

                    override fun onNavigationBarHide(orientation: Int) {
                        topBar.setPadding(0, 0, 0, 0)
                    }
                })
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_del) {
            showDeleteDialog()
        } else if (id == R.id.btn_back) {
            onBackPressed()
        }
    }

    /** 是否删除此张图片  */
    private fun showDeleteDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("提示")
        builder.setMessage("要删除这张照片吗？")
        builder.setNegativeButton("取消", null)
        builder.setPositiveButton("确定") { dialog, which -> //移除当前图片刷新界面
            mImageItems!!.removeAt(mCurrentPosition)
            if (mImageItems!!.size > 0) {
                mAdapter!!.setData(mImageItems!!)
                mAdapter!!.notifyDataSetChanged()
                mTitleCount!!.text = getString(R.string.ip_preview_image_count, mCurrentPosition + 1, mImageItems!!.size)
            } else {
                onBackPressed()
            }
        }
        builder.show()
    }

    override fun onBackPressed() {
        val intent = Intent()
        //带回最新数据
        intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, mImageItems)
        setResult(ImagePicker.RESULT_CODE_BACK, intent)
        finish()
        super.onBackPressed()
    }

    /** 单击时，隐藏头和尾  */
    override fun onImageSingleTap() {
        if (topBar.visibility == View.VISIBLE) {
            topBar.animation = AnimationUtils.loadAnimation(this, R.anim.top_out)
            topBar.visibility = View.GONE
            tintManager!!.setStatusBarTintResource(Color.TRANSPARENT) //通知栏所需颜色
            //给最外层布局加上这个属性表示，Activity全屏显示，且状态栏被隐藏覆盖掉。
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            topBar.animation = AnimationUtils.loadAnimation(this, R.anim.top_in)
            topBar.visibility = View.VISIBLE
            tintManager!!.setStatusBarTintResource(R.color.ip_color_primary_dark) //通知栏所需颜色
            //Activity全屏显示，但状态栏不会被隐藏覆盖，状态栏依然可见，Activity顶端布局部分会被状态遮住
//            if (Build.VERSION.SDK_INT >= 16) content.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }
}