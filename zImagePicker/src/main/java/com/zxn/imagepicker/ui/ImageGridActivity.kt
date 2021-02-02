package com.zxn.imagepicker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zxn.imagepicker.DataHolder
import com.zxn.imagepicker.DataHolder.save
import com.zxn.imagepicker.ImageDataSource
import com.zxn.imagepicker.ImageDataSource.OnImagesLoadedListener
import com.zxn.imagepicker.ImagePicker
import com.zxn.imagepicker.ImagePicker.OnImageSelectedListener
import com.zxn.imagepicker.R
import com.zxn.imagepicker.adapter.ImageFolderAdapter
import com.zxn.imagepicker.adapter.ImageRecyclerAdapter
import com.zxn.imagepicker.adapter.ImageRecyclerAdapter.OnImageItemClickListener
import com.zxn.imagepicker.bean.ImageFolder
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.ui.ImageGridActivity
import com.zxn.imagepicker.ui.ImagePreviewActivity
import com.zxn.imagepicker.util.Utils
import com.zxn.imagepicker.view.FolderPopUpWindow
import com.zxn.imagepicker.view.GridSpacingItemDecoration
import java.util.*
import kotlin.collections.ArrayList

/**
 * 照片选择页面.
 */
class ImageGridActivity : ImageBaseActivity(),
        OnImagesLoadedListener,
        OnImageItemClickListener,
        OnImageSelectedListener,
        View.OnClickListener {

    private var imagePicker: ImagePicker = ImagePicker

    private var isOrigin = false //是否选中原图
    private var mFooterBar //底部栏
            : View? = null
    private var mBtnOk //确定按钮
            : Button? = null
    private lateinit var mllDir //文件夹切换按钮
            : View
    private var mtvDir //显示当前文件夹
            : TextView? = null
    private var mBtnPre //预览按钮
            : TextView? = null
    private var mImageFolderAdapter //图片文件夹的适配器
            : ImageFolderAdapter? = null
    private var mFolderPopupWindow //ImageSet的PopupWindow
            : FolderPopUpWindow? = null

    //所有的图片文件夹
    private var mImageFolders: MutableList<ImageFolder> = ArrayList()

    //    private ImageGridAdapter mImageGridAdapter;  //图片九宫格展示的适配器
    private var directPhoto = false // 默认不是直接调取相机
    private var mRecyclerView: RecyclerView? = null
    private var mRecyclerAdapter: ImageRecyclerAdapter? = null
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        directPhoto = savedInstanceState.getBoolean(EXTRAS_TAKE_PICKERS, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(EXTRAS_TAKE_PICKERS, directPhoto)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_grid)
        imagePicker = ImagePicker
        imagePicker.clear()
        imagePicker.addOnImageSelectedListener(this)
        val data = intent
        // 新增可直接拍照
        if (data != null && data.extras != null) {
            directPhoto = data.getBooleanExtra(EXTRAS_TAKE_PICKERS, false) // 默认不是直接打开相机
            if (directPhoto) {
                if (!checkPermission(Manifest.permission.CAMERA)) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA)
                } else {
                    imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE)
                }
            }
            val images = data.getSerializableExtra(EXTRAS_IMAGES) as ArrayList<ImageItem>
            imagePicker.selectedImages = images
        }
        mRecyclerView = findViewById<View>(R.id.recycler) as RecyclerView
        findViewById<View>(R.id.btn_back).setOnClickListener(this)
        mBtnOk = findViewById<View>(R.id.btn_ok) as Button
        mBtnOk!!.setOnClickListener(this)
        mBtnPre = findViewById<View>(R.id.btn_preview) as TextView
        mBtnPre!!.setOnClickListener(this)
        mFooterBar = findViewById(R.id.footer_bar)
        mllDir = findViewById(R.id.ll_dir)
        mllDir.setOnClickListener(this)
        mtvDir = findViewById<View>(R.id.tv_dir) as TextView
        if (imagePicker.isMultiMode) {
            mBtnOk!!.visibility = View.VISIBLE
            mBtnPre!!.visibility = View.VISIBLE
        } else {
            mBtnOk!!.visibility = View.GONE
            mBtnPre!!.visibility = View.GONE
        }

//        mImageGridAdapter = new ImageGridAdapter(this, null);
        mImageFolderAdapter = ImageFolderAdapter(this, null)
        mRecyclerAdapter = ImageRecyclerAdapter(this, null)
        onImageSelected(0, null, false)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ImageDataSource(this, null, this)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION_STORAGE)
            }
        } else {
            ImageDataSource(this, null, this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_STORAGE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImageDataSource(this, null, this)
            } else {
                showToast("权限被禁止，无法选择本地图片")
            }
        } else if (requestCode == REQUEST_PERMISSION_CAMERA) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                imagePicker!!.takePicture(this, ImagePicker.REQUEST_CODE_TAKE)
            } else {
                showToast("权限被禁止，无法打开相机")
            }
        }
    }

    override fun onDestroy() {
        imagePicker!!.removeOnImageSelectedListener(this)
        super.onDestroy()
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.btn_ok) {
            val intent = Intent()
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker!!.selectedImages)
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //多选不允许裁剪裁剪，返回数据
            finish()
        } else if (id == R.id.ll_dir) {
            if (mImageFolders == null) {
                Log.i("ImageGridActivity", "您的手机没有图片")
                return
            }
            //点击文件夹按钮
            createPopupFolderList()
            mImageFolderAdapter!!.refreshData(mImageFolders) //刷新数据
            if (mFolderPopupWindow!!.isShowing) {
                mFolderPopupWindow!!.dismiss()
            } else {
                mFolderPopupWindow!!.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0)
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                var index = mImageFolderAdapter!!.selectIndex
                index = if (index == 0) index else index - 1
                mFolderPopupWindow!!.setSelection(index)
            }
        } else if (id == R.id.btn_preview) {
            val intent = Intent(this@ImageGridActivity, ImagePreviewActivity::class.java)
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0)
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker!!.selectedImages)
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin)
            intent.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true)
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW)
        } else if (id == R.id.btn_back) {
            //点击返回按钮
            finish()
        }
    }

    /**
     * 创建弹出的ListView
     */
    private fun createPopupFolderList() {
        mFolderPopupWindow = FolderPopUpWindow(this, mImageFolderAdapter)
        mFolderPopupWindow!!.setOnItemClickListener(object : FolderPopUpWindow.OnItemClickListener {

            override fun onItemClick(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long) {
                mImageFolderAdapter!!.selectIndex = position
                imagePicker.currentImageFolderPosition = position
                mFolderPopupWindow!!.dismiss()
                val imageFolder = adapterView?.adapter?.getItem(position) as ImageFolder
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mRecyclerAdapter!!.refreshData(imageFolder.images)
                    mtvDir!!.text = imageFolder.name
                }
            }

            /*override fun onItemClick(adapterView: AdapterView<*>, view: View?, position: Int, l: Long) {
                mImageFolderAdapter!!.selectIndex = position
                imagePicker!!.currentImageFolderPosition = position
                mFolderPopupWindow!!.dismiss()
                val imageFolder = adapterView.adapter.getItem(position) as ImageFolder
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mRecyclerAdapter!!.refreshData(imageFolder.images)
                    mtvDir!!.text = imageFolder.name
                }
            }*/
        })
        mFolderPopupWindow!!.setMargin(mFooterBar!!.height)
    }



    override fun onImageItemClick(view: View?, imageItem: ImageItem?, position: Int) {
        //根据是否有相机按钮确定位置
        var position = position
        position = if (imagePicker!!.isShowCamera) position - 1 else position
        if (imagePicker!!.isMultiMode) {
            val intent = Intent(this@ImageGridActivity, ImagePreviewActivity::class.java)
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position)
            /**
             * 2017-03-20
             *
             * 依然采用弱引用进行解决，采用单例加锁方式处理
             */

            // 据说这样会导致大量图片的时候崩溃
//            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getCurrentImageFolderItems());

            // 但采用弱引用会导致预览弱引用直接返回空指针
            save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imagePicker!!.currentImageFolderItems)
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin)
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW) //如果是多选，点击图片进入预览界面
        } else {
            imagePicker!!.clearSelectedImages()
            imagePicker!!.addSelectedImageItem(position, imagePicker!!.currentImageFolderItems[position], true)
            if (imagePicker!!.isCrop) {
                val intent = Intent(this@ImageGridActivity, ImageCropActivity::class.java)
                startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP) //单选需要裁剪，进入裁剪界面
            } else {
                val intent = Intent()
                intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker!!.selectedImages)
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //单选不需要裁剪，返回数据
                finish()
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    override fun onImageSelected(position: Int, item: ImageItem?, isAdd: Boolean) {
        if (imagePicker!!.selectImageCount > 0) {
            mBtnOk!!.text = getString(R.string.ip_select_complete, imagePicker!!.selectImageCount, imagePicker!!.selectLimit)
            mBtnOk!!.isEnabled = true
            mBtnPre!!.isEnabled = true
            mBtnPre!!.text = resources.getString(R.string.ip_preview_count, imagePicker!!.selectImageCount)
            mBtnPre!!.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted))
            mBtnOk!!.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted))
        } else {
            mBtnOk!!.text = getString(R.string.ip_complete)
            mBtnOk!!.isEnabled = false
            mBtnPre!!.isEnabled = false
            mBtnPre!!.text = resources.getString(R.string.ip_preview)
            mBtnPre!!.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted))
            mBtnOk!!.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted))
        }
        //        mImageGridAdapter.notifyDataSetChanged();
//        mRecyclerAdapter.notifyItemChanged(position); // 17/4/21 fix the position while click img to preview
//        mRecyclerAdapter.notifyItemChanged(position + (imagePicker.isShowCamera() ? 1 : 0));// 17/4/24  fix the position while click right bottom preview button

        for (i in
        (if (imagePicker.isShowCamera) 1 else 0) until mRecyclerAdapter!!.itemCount) {
            if (mRecyclerAdapter!!.getItem(i)!!.path != null && mRecyclerAdapter!!.getItem(i)!!.path == item?.path) {
                mRecyclerAdapter!!.notifyItemChanged(i)
                return
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && data.extras != null) {
            if (resultCode == ImagePicker.RESULT_CODE_BACK) {
                isOrigin = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false)
            } else {
                //从拍照界面返回
                //点击 X , 没有选择照片
                if (data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
                    //什么都不做 直接调起相机
                } else {
                    //说明是从裁剪页面过来的数据，直接返回就可以
                    setResult(ImagePicker.RESULT_CODE_ITEMS, data)
                }
                finish()
            }
        } else {
            //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
            if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
                //发送广播通知图片增加了
                ImagePicker.galleryAddPic(this, imagePicker!!.takeImageFile)
                /**
                 * 2017-03-21 对机型做旋转处理
                 */
                val path = imagePicker!!.takeImageFile?.absolutePath
                //                int degree = BitmapUtil.getBitmapDegree(path);
//                if (degree != 0){
//                    Bitmap bitmap = BitmapUtil.rotateBitmapByDegree(path,degree);
//                    if (bitmap != null){
//                        File file = new File(path);
//                        try {
//                            FileOutputStream bos = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//                            bos.flush();
//                            bos.close();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }
                val imageItem = ImageItem()
                imageItem.path = path
                imagePicker!!.clearSelectedImages()
                imagePicker!!.addSelectedImageItem(0, imageItem, true)
                if (imagePicker!!.isCrop) {
                    val intent = Intent(this@ImageGridActivity, ImageCropActivity::class.java)
                    startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP) //单选需要裁剪，进入裁剪界面
                } else {
                    val intent = Intent()
                    intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker!!.selectedImages)
                    setResult(ImagePicker.RESULT_CODE_ITEMS, intent) //单选不需要裁剪，返回数据
                    finish()
                }
            } else if (directPhoto) {
                finish()
            }
        }
    }

    companion object {
        const val REQUEST_PERMISSION_STORAGE = 0x01
        const val REQUEST_PERMISSION_CAMERA = 0x02
        const val EXTRAS_TAKE_PICKERS = "TAKE"
        const val EXTRAS_IMAGES = "IMAGES"

        /**
         * 直接进入相册选取页面,并获取结果回调数据.
         *
         * @param fragment    activity
         * @param images      已经选择过照片集合.
         * @param requestCode 请求码.
         */
        fun jumpToForResult(fragment: Fragment, images: ArrayList<ImageItem?>?, requestCode: Int) {
            val intent = Intent(fragment.context, ImageGridActivity::class.java)
            intent.putExtra(EXTRAS_IMAGES, images)
            fragment.startActivityForResult(intent, requestCode)
        }

        /**
         * 直接进入相册选取页面,并获取结果回调数据.
         *
         * @param activity    activity
         * @param images      已经选择过照片集合.
         * @param requestCode 请求码.
         */
        fun jumpToForResult(activity: Activity, images: ArrayList<ImageItem>?, requestCode: Int) {
            val intent = Intent(activity, ImageGridActivity::class.java)
            intent.putExtra(EXTRAS_IMAGES, images)
            activity.startActivityForResult(intent, requestCode)
        }

        /**
         * 直接进入相册选取页面,并获取结果回调数据.
         *
         * @param activity
         */
        fun jumpToForResult(activity: Activity, requestCode: Int) {
            val intent = Intent(activity, ImageGridActivity::class.java)
            activity.startActivityForResult(intent, requestCode)
        }

        fun jumpToForResult(fragment: Fragment, requestCode: Int) {
            val intent = Intent(fragment.context, ImageGridActivity::class.java)
            fragment.startActivityForResult(intent, requestCode)
        }

//        /**
//         * 进入相册选取页面,并获取结果回调数据.
//         *
//         * @param activity    activity
//         * @param directPhoto true:直接打开相机,false:不打开相机进入相册.默认直接相册.
//         * @param requestCode
//         */
//        fun jumpToForResult(activity: Activity, directPhoto: Boolean, requestCode: Int) {
//            val intent = Intent(activity, ImageGridActivity::class.java)
//            if (directPhoto) {
//                intent.putExtra(EXTRAS_TAKE_PICKERS, directPhoto)
//            }
//            activity.startActivityForResult(intent, requestCode)
//        }

        fun jumpToForResult(fragment: Fragment, directPhoto: Boolean, requestCode: Int) {
            val intent = Intent(fragment.context, ImageGridActivity::class.java)
            if (directPhoto) {
                intent.putExtra(EXTRAS_TAKE_PICKERS, directPhoto)
            }
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    override fun onImagesLoaded(imageFolders: MutableList<ImageFolder>) {
        mImageFolders = imageFolders
        imagePicker.imageFolders = imageFolders
        if (imageFolders.size == 0) {
            mRecyclerAdapter!!.refreshData(null)
        } else {
            mRecyclerAdapter!!.refreshData(imageFolders[0].images)
        }
        mRecyclerAdapter!!.setOnImageItemClickListener(this)
        mRecyclerView!!.layoutManager = GridLayoutManager(this, 3)
        mRecyclerView!!.addItemDecoration(GridSpacingItemDecoration(3, Utils.dp2px(this, 2f), false))
        mRecyclerView!!.adapter = mRecyclerAdapter
        mImageFolderAdapter!!.refreshData(imageFolders)
    }

}