package com.lzy.imagepickerdemo.wxdemo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lzy.imagepickerdemo.R
import com.lzy.imagepickerdemo.SelectDialog
import com.lzy.imagepickerdemo.SelectDialog.SelectDialogListener
import com.lzy.imagepickerdemo.imageloader.GlideImageLoader
import com.lzy.imagepickerdemo.wxdemo.ImagePickerAdapter.OnRecyclerViewItemClickListener
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.ui.ImageGridActivity
import com.zxn.imagepicker.ui.ImagePreviewDelActivity
import com.zxn.imagepicker.view.CropImageView
import java.util.*

/**
 * demo演示.
 * Created by Ny on 2021/2/1.
 */
class WxDemoActivity : AppCompatActivity(), OnRecyclerViewItemClickListener {
    private var selImageList //当前选择的所有图片
            : ArrayList<ImageItem>? = null
    private val maxImgCount = 8 //允许选择图片最大数
    private var adapter: ImagePickerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wxdemo)

        //最好放到 Application oncreate执行
        initImagePicker()
        initWidget()
    }

    private fun initImagePicker() {
        val imagePicker = ImagePicker.apply {
            setshowSelectIndex(false)
            checkBoxResource = R.drawable.picker_sc_item_checked
        }
        imagePicker.imageLoader = GlideImageLoader() //设置图片加载器
        imagePicker.isShowCamera = true //显示拍照按钮
        imagePicker.isCrop = true //允许裁剪（单选才有效）
        imagePicker.isSaveRectangle = true //是否按矩形区域保存
        imagePicker.selectLimit = maxImgCount //选中数量限制
        imagePicker.style = CropImageView.Style.RECTANGLE //裁剪框的形状
        imagePicker.focusWidth = 800 //裁剪框的宽度。单位像素（圆形自动取宽高最小值）
        imagePicker.focusHeight = 800 //裁剪框的高度。单位像素（圆形自动取宽高最小值）
        imagePicker.outPutX = 1000 //保存文件的宽度。单位像素
        imagePicker.outPutY = 1000 //保存文件的高度。单位像素
    }

    private fun initWidget() {
        val recyclerView = findViewById<View>(R.id.recyclerView) as RecyclerView
        selImageList = ArrayList()
        adapter = ImagePickerAdapter(this, selImageList, maxImgCount)
        adapter!!.setOnItemClickListener(this)
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter
    }

    private fun showBottomDialog(listener: SelectDialogListener, names: List<String>): SelectDialog {
        val dialog = SelectDialog(this, R.style.transparentFrameWindowStyle,
                listener, names)
        if (!this.isFinishing) {
            dialog.show()
        }
        return dialog
    }

    override fun onItemClick(view: View, position: Int) {
        when (position) {
            IMAGE_ITEM_ADD -> {
                val names: MutableList<String> = ArrayList()
                names.add("拍照")
                names.add("相册")
                showBottomDialog(object : SelectDialogListener {
                    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                        when (position) {
                            0 -> {
//                                *//**
//                                * 0.4.7 目前直接调起相机不支持裁剪，如果开启裁剪后不会返回图片，请注意，后续版本会解决
//                                *
//                                * 但是当前直接依赖的版本已经解决，考虑到版本改动很少，所以这次没有上传到远程仓库
//                                *
//                                * 如果实在有所需要，请直接下载源码引用。
//                                *//*
//                                *//**
//                                * 0.4.7 目前直接调起相机不支持裁剪，如果开启裁剪后不会返回图片，请注意，后续版本会解决
//                                *
//                                * 但是当前直接依赖的版本已经解决，考虑到版本改动很少，所以这次没有上传到远程仓库
//                                *
//                                * 如果实在有所需要，请直接下载源码引用。
//                                *//*
                                //打开选择,本次允许选择的数量
                                ImagePicker.selectLimit = maxImgCount - selImageList!!.size
                                val intent = Intent(this@WxDemoActivity, ImageGridActivity::class.java)
                                intent.putExtra(ImageGridActivity.EXTRAS_TAKE_PICKERS, true) // 是否是直接打开相机
                                startActivityForResult(intent, REQUEST_CODE_SELECT)
                            }
                            1 -> {
                                //打开选择,本次允许选择的数量
                                ImagePicker.selectLimit = maxImgCount - selImageList!!.size
                                val intent1 = Intent(this@WxDemoActivity, ImageGridActivity::class.java)
//                                *//* 如果需要进入选择的时候显示已经选中的图片，
//                                * 详情请查看ImagePickerActivity
//                                * *//*
//                                intent1.putExtra(ImageGridActivity.EXTRAS_IMAGES,images);
                                startActivityForResult(intent1, REQUEST_CODE_SELECT)
                            }
                            else -> {
                            }
                        }
                    }
                }, names)
            }
            else -> {
                //打开预览
                val intentPreview = Intent(this, ImagePreviewDelActivity::class.java)
                intentPreview.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, adapter!!.images as ArrayList<ImageItem?>)
                intentPreview.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position)
                intentPreview.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true)
                startActivityForResult(intentPreview, REQUEST_CODE_PREVIEW)
            }
        }
    }

    var images: ArrayList<ImageItem>? = null
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ImagePicker.RESULT_CODE_ITEMS) {
            //添加图片返回
            if (data != null && requestCode == REQUEST_CODE_SELECT) {
                images = data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) as ArrayList<ImageItem>?
                if (images != null) {
                    selImageList!!.addAll(images!!)
                    adapter!!.images = selImageList
                }
            }
        } else if (resultCode == ImagePicker.RESULT_CODE_BACK) {
            //预览图片返回
            if (data != null && requestCode == REQUEST_CODE_PREVIEW) {
                images = data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_ITEMS) as ArrayList<ImageItem>?
                if (images != null) {
                    selImageList!!.clear()
                    selImageList!!.addAll(images!!)
                    adapter!!.images = selImageList
                }
            }
        }
    }

    companion object {
        const val IMAGE_ITEM_ADD = -1
        const val REQUEST_CODE_SELECT = 100
        const val REQUEST_CODE_PREVIEW = 101
    }
}