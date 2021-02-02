package com.zxn.imagepicker.ui

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.R
import com.zxn.imagepicker.view.SystemBarTintManager

/**
 *
 */
open class ImageBaseActivity : AppCompatActivity() {
    @JvmField
    protected var tintManager: SystemBarTintManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true)
        }
        tintManager = SystemBarTintManager(this)
        tintManager!!.isStatusBarTintEnabled = true
        tintManager!!.setStatusBarTintResource(R.color.ip_color_primary_dark) //设置上方状态栏的颜色
    }

    @TargetApi(19)
    private fun setTranslucentStatus(on: Boolean) {
        val win = window
        val winParams = win.attributes
        val bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    fun checkPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun showToast(toastText: String?) {
        Toast.makeText(applicationContext, toastText, Toast.LENGTH_SHORT).show()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        ImagePicker.restoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        ImagePicker.saveInstanceState(outState)
    }
}