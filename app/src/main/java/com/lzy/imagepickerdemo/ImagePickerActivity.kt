package com.lzy.imagepickerdemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import com.lzy.imagepickerdemo.imageloader.GlideImageLoader
import com.lzy.imagepickerdemo.imageloader.PicassoImageLoader
import com.lzy.imagepickerdemo.imageloader.UILImageLoader
import com.lzy.imagepickerdemo.imageloader.XUtils3ImageLoader
import com.lzy.imagepickerdemo.wxdemo.WxDemoActivity
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.ui.ImageGridActivity
import com.zxn.imagepicker.view.CropImageView
import java.util.*

/**
 * Created by ny on 2021/2/2.
 */
class ImagePickerActivity : AppCompatActivity(), OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private var imagePicker: ImagePicker = ImagePicker

    private var rb_uil: RadioButton? = null
    private var rb_glide: RadioButton? = null
    private var rb_picasso: RadioButton? = null
    private var rb_fresco: RadioButton? = null
    private var rb_xutils3: RadioButton? = null
    private var rb_xutils: RadioButton? = null
    private var rb_single_select: RadioButton? = null
    private var rb_muti_select: RadioButton? = null
    private var rb_crop_square: RadioButton? = null
    private var rb_crop_circle: RadioButton? = null
    private var tv_select_limit: TextView? = null
    private var gridView: GridView? = null
    private var et_crop_width: EditText? = null
    private var et_crop_height: EditText? = null
    private var et_crop_radius: EditText? = null
    private var et_outputx: EditText? = null
    private var et_outputy: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_picker)

        imagePicker.setImageLoader(GlideImageLoader())
        rb_uil = findViewById<View>(R.id.rb_uil) as RadioButton
        rb_glide = findViewById<View>(R.id.rb_glide) as RadioButton
        rb_picasso = findViewById<View>(R.id.rb_picasso) as RadioButton
        rb_fresco = findViewById<View>(R.id.rb_fresco) as RadioButton
        rb_xutils3 = findViewById<View>(R.id.rb_xutils3) as RadioButton
        rb_xutils = findViewById<View>(R.id.rb_xutils) as RadioButton
        rb_single_select = findViewById<View>(R.id.rb_single_select) as RadioButton
        rb_muti_select = findViewById<View>(R.id.rb_muti_select) as RadioButton
        rb_crop_square = findViewById<View>(R.id.rb_crop_square) as RadioButton
        rb_crop_circle = findViewById<View>(R.id.rb_crop_circle) as RadioButton
        rb_glide!!.isChecked = true
        rb_muti_select!!.isChecked = true
        rb_crop_square!!.isChecked = true
        et_crop_width = findViewById<View>(R.id.et_crop_width) as EditText
        et_crop_width!!.setText("280")
        et_crop_height = findViewById<View>(R.id.et_crop_height) as EditText
        et_crop_height!!.setText("280")
        et_crop_radius = findViewById<View>(R.id.et_crop_radius) as EditText
        et_crop_radius!!.setText("140")
        et_outputx = findViewById<View>(R.id.et_outputx) as EditText
        et_outputx!!.setText("800")
        et_outputy = findViewById<View>(R.id.et_outputy) as EditText
        et_outputy!!.setText("800")
        tv_select_limit = findViewById<View>(R.id.tv_select_limit) as TextView
        val sb_select_limit = findViewById<View>(R.id.sb_select_limit) as SeekBar
        sb_select_limit.max = 15
        sb_select_limit.setOnSeekBarChangeListener(this)
        sb_select_limit.progress = 9
        val cb_show_camera = findViewById<View>(R.id.cb_show_camera) as CheckBox
        cb_show_camera.setOnCheckedChangeListener(this)
        cb_show_camera.isChecked = true
        val cb_crop = findViewById<View>(R.id.cb_crop) as CheckBox
        cb_crop.setOnCheckedChangeListener(this)
        cb_crop.isChecked = true
        val cb_isSaveRectangle = findViewById<View>(R.id.cb_isSaveRectangle) as CheckBox
        cb_isSaveRectangle.setOnCheckedChangeListener(this)
        cb_isSaveRectangle.isChecked = true
        val btn_open_gallery = findViewById<View>(R.id.btn_open_gallery) as Button
        btn_open_gallery.setOnClickListener(this)
        val btn_wxDemo = findViewById<View>(R.id.btn_wxDemo) as Button
        btn_wxDemo.setOnClickListener(this)
        gridView = findViewById<View>(R.id.gridview) as GridView
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_open_gallery -> {
                if (rb_uil!!.isChecked) imagePicker!!.imageLoader = UILImageLoader() else if (rb_glide!!.isChecked) imagePicker!!.imageLoader = GlideImageLoader() else if (rb_picasso!!.isChecked) imagePicker!!.imageLoader = PicassoImageLoader() else if (rb_fresco!!.isChecked) imagePicker!!.imageLoader = GlideImageLoader() else if (rb_xutils3!!.isChecked) imagePicker!!.imageLoader = XUtils3ImageLoader() else if (rb_xutils!!.isChecked) imagePicker!!.imageLoader = GlideImageLoader()
                if (rb_single_select!!.isChecked) imagePicker!!.isMultiMode = false else if (rb_muti_select!!.isChecked) imagePicker!!.isMultiMode = true
                if (rb_crop_square!!.isChecked) {
                    imagePicker!!.style = CropImageView.Style.RECTANGLE
                    var width = Integer.valueOf(et_crop_width!!.text.toString())
                    var height = Integer.valueOf(et_crop_height!!.text.toString())
                    width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width.toFloat(), resources.displayMetrics).toInt()
                    height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height.toFloat(), resources.displayMetrics).toInt()
                    imagePicker!!.focusWidth = width
                    imagePicker!!.focusHeight = height
                } else if (rb_crop_circle!!.isChecked) {
                    imagePicker.style = CropImageView.Style.CIRCLE
                    var radius = Integer.valueOf(et_crop_radius!!.text.toString())
                    radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius.toFloat(), resources.displayMetrics).toInt()
                    imagePicker!!.focusWidth = radius * 2
                    imagePicker!!.focusHeight = radius * 2
                }
                imagePicker!!.outPutX = Integer.valueOf(et_outputx!!.text.toString())
                imagePicker!!.outPutY = Integer.valueOf(et_outputy!!.text.toString())
                ImageGridActivity.jumpToForResult(this, images, 100)
            }
            R.id.btn_wxDemo -> startActivity(Intent(this, WxDemoActivity::class.java))
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.cb_show_camera -> imagePicker!!.isShowCamera = isChecked
            R.id.cb_crop -> imagePicker!!.isCrop = isChecked
            R.id.cb_isSaveRectangle -> imagePicker!!.isSaveRectangle = isChecked
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        tv_select_limit!!.text = progress.toString()
        imagePicker!!.selectLimit = progress
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}
    override fun onStopTrackingTouch(seekBar: SeekBar) {}
    var images: ArrayList<ImageItem>? = null
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            if (data != null && requestCode == 100) {
                images = data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) as ArrayList<ImageItem>?
                val adapter = MyAdapter(images)
                gridView!!.adapter = adapter
            } else {
                Toast.makeText(this, "没有数据", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private inner class MyAdapter(private var items: List<ImageItem>?) : BaseAdapter() {
        fun setData(items: List<ImageItem>?) {
            this.items = items
            notifyDataSetChanged()
        }

        override fun getCount(): Int {
            return items!!.size
        }

        override fun getItem(position: Int): ImageItem {
            return items!![position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
            val imageView: ImageView
            val size = gridView!!.width / 3
            if (convertView == null) {
                imageView = ImageView(this@ImagePickerActivity)
                val params = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, size)
                imageView.layoutParams = params
                imageView.setBackgroundColor(Color.parseColor("#88888888"))
            } else {
                imageView = convertView as ImageView
            }
            imagePicker!!.imageLoader?.displayImage(this@ImagePickerActivity, getItem(position).path, imageView, size, size)
            return imageView
        }
    }
}