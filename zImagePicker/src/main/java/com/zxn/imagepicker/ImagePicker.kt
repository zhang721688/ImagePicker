package com.zxn.imagepicker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.zxn.imagepicker.bean.ImageFolder
import com.zxn.imagepicker.bean.ImageItem
import com.zxn.imagepicker.loader.ImageLoader
import com.zxn.imagepicker.util.ProviderUtil
import com.zxn.imagepicker.util.Utils
import com.zxn.imagepicker.view.CropImageView
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * ================================================
 * 采用单例和弱引用解决Intent传值限制导致的异常
 * ================================================
 */
object ImagePicker /*private constructor()*/ {

    val TAG = ImagePicker::class.java.simpleName

    const val REQUEST_CODE_TAKE = 1001
    const val REQUEST_CODE_CROP = 1002
    const val REQUEST_CODE_PREVIEW = 1003
    const val RESULT_CODE_ITEMS = 1004
    const val RESULT_CODE_BACK = 1005
    const val EXTRA_RESULT_ITEMS = "extra_result_items"
    const val EXTRA_SELECTED_IMAGE_POSITION = "selected_image_position"
    const val EXTRA_IMAGE_ITEMS = "extra_image_items"
    const val EXTRA_FROM_ITEMS = "extra_from_items"


    var isMultiMode = true //图片选择模式
    var selectLimit = 9 //最大选择图片数量
    var isCrop = true //裁剪
    var isShowCamera = true //显示相机
    var isSaveRectangle = false //裁剪后的图片是否是矩形，否者跟随裁剪框的形状
    var outPutX = 800 //裁剪保存宽度
    var outPutY = 800 //裁剪保存高度
    var focusWidth = 280 //焦点框的宽度

    var focusHeight = 280 //焦点框的高度


    //图片加载器
    var imageLoader
            : ImageLoader? = null

    var style: CropImageView.Style = CropImageView.Style.RECTANGLE //裁剪框的形状

    private var cropCacheFolder: File? = null
    var takeImageFile: File? = null
        private set
    var cropBitmap: Bitmap? = null

    /**
     * 是否显示已经选图片的角标.
     */
    var isShowSelectIndex = false
        private set

    fun setshowSelectIndex(show: Boolean): ImagePicker {
        isShowSelectIndex = show
        return this
    }

    var checkBoxResource = 0

    //选中的图片集合
    private var mSelectedImages: ArrayList<ImageItem> = ArrayList()

    //所有的图片文件夹
    private var mImageFolders
            : MutableList<ImageFolder> = mutableListOf()

    var currentImageFolderPosition = 0 //当前选中的文件夹位置 0表示所有图片
        //private set

    private var mImageSelectedListeners // 图片选中的监听回调
            : MutableList<OnImageSelectedListener>? = null

    fun setMultiMode(multiMode: Boolean): ImagePicker {
        isMultiMode = multiMode
        return this
    }

    fun setSelectLimit(selectLimit: Int): ImagePicker {
        this.selectLimit = selectLimit
        return this
    }

    fun setCrop(crop: Boolean): ImagePicker {
        isCrop = crop
        return this
    }

    fun setShowCamera(showCamera: Boolean): ImagePicker {
        isShowCamera = showCamera
        return this
    }

    fun setSaveRectangle(isSaveRectangle: Boolean): ImagePicker {
        this.isSaveRectangle = isSaveRectangle
        return this
    }

    fun setOutPutX(outPutX: Int): ImagePicker {
        this.outPutX = outPutX
        return this
    }

    fun setOutPutY(outPutY: Int): ImagePicker {
        this.outPutY = outPutY
        return this
    }

    fun setFocusWidth(focusWidth: Int): ImagePicker {
        this.focusWidth = focusWidth
        return this
    }

    fun setFocusHeight(focusHeight: Int): ImagePicker {
        this.focusHeight = focusHeight
        return this
    }

    fun getCropCacheFolder(context: Context): File {
        if (cropCacheFolder == null) {
            cropCacheFolder = File(context.cacheDir.toString() + "/ImagePicker/cropTemp/")
        }
        return cropCacheFolder!!
    }

    fun setCropCacheFolder(cropCacheFolder: File?): ImagePicker {
        this.cropCacheFolder = cropCacheFolder
        return this
    }

    fun setImageLoader(imageLoader: ImageLoader): ImagePicker {
        this.imageLoader = imageLoader
        return this
    }

    fun setStyle(style: CropImageView.Style): ImagePicker {
        this.style = style
        return this
    }

     var imageFolders: List<ImageFolder>  = mImageFolders
        //get() = mImageFolders

    fun setImageFolders(imageFolders: MutableList<ImageFolder>): ImagePicker {
        mImageFolders = imageFolders
        return this
    }

    fun setCurrentImageFolderPosition(mCurrentSelectedImageSetPosition: Int): ImagePicker {
        currentImageFolderPosition = mCurrentSelectedImageSetPosition
        return this
    }

    /**
     * 是否展示视频
     *
     * @param showVideo
     * @return
     */
    fun showVideo(showVideo: Boolean): ImagePicker {
        //ConfigManager.getInstance().setShowVideo(showVideo);
        return this
    }

    val currentImageFolderItems: ArrayList<ImageItem>
        get() = mImageFolders!![currentImageFolderPosition].images

    fun isSelect(item: ImageItem): Boolean {
        return mSelectedImages.contains(item)
    }

    val selectImageCount: Int
        get() = if (mSelectedImages == null) {
            0
        } else mSelectedImages!!.size
    var selectedImages: ArrayList<ImageItem>
        get() = mSelectedImages
        set(selectedImages) {
            if (selectedImages == null) {
                return
            }
            mSelectedImages = selectedImages
        }

    fun clearSelectedImages() {
        if (mSelectedImages != null) mSelectedImages!!.clear()
    }

    fun clear() {
        if (mImageSelectedListeners != null) {
            mImageSelectedListeners!!.clear()
            mImageSelectedListeners = null
        }
        if (mImageFolders != null) {
            mImageFolders!!.clear()
            //mImageFolders = null
        }
        if (mSelectedImages != null) {
            mSelectedImages!!.clear()
        }
        currentImageFolderPosition = 0
    }

    /**
     * 拍照的方法
     */
    fun takePicture(activity: Activity, requestCode: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
            takeImageFile = if (Utils.existSDCard()) File(Environment.getExternalStorageDirectory(), "/DCIM/camera/") else Environment.getDataDirectory()
            takeImageFile = createFile(takeImageFile, "IMG_", ".jpg")
            if (takeImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！
                val uri: Uri
                if (VERSION.SDK_INT <= VERSION_CODES.M) {
                    uri = Uri.fromFile(takeImageFile)
                } else {
                    /**
                     * 7.0 调用系统相机拍照不再允许使用Uri方式，应该替换为FileProvider
                     * 并且这样可以解决MIUI系统上拍照返回size为0的情况
                     */
                    uri = FileProvider.getUriForFile(activity, ProviderUtil.getFileProviderName(activity), takeImageFile!!)
                    //加入uri权限 要不三星手机不能拍照
                    val resInfoList = activity.packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY)
                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        activity.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
                Log.e("nanchen", ProviderUtil.getFileProviderName(activity))
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            }
        }
        activity.startActivityForResult(takePictureIntent, requestCode)
    }

    /**
     * 图片选中的监听
     */
    interface OnImageSelectedListener {
        fun onImageSelected(position: Int, item: ImageItem?, isAdd: Boolean)
    }

    fun addOnImageSelectedListener(l: OnImageSelectedListener) {
        if (mImageSelectedListeners == null) mImageSelectedListeners = ArrayList()
        mImageSelectedListeners!!.add(l)
    }

    fun removeOnImageSelectedListener(l: OnImageSelectedListener) {
        if (mImageSelectedListeners == null) return
        mImageSelectedListeners!!.remove(l)
    }

    fun addSelectedImageItem(position: Int, item: ImageItem, isAdd: Boolean) {
        if (isAdd) mSelectedImages!!.add(item) else mSelectedImages!!.remove(item)
        notifyImageSelectedChanged(position, item, isAdd)
    }

    private fun notifyImageSelectedChanged(position: Int, item: ImageItem, isAdd: Boolean) {
        if (mImageSelectedListeners == null) return
        for (l in mImageSelectedListeners!!) {
            l.onImageSelected(position, item, isAdd)
        }
    }

    /**
     * 用于手机内存不足，进程被系统回收，重启时的状态恢复
     */
    fun restoreInstanceState(savedInstanceState: Bundle) {
        cropCacheFolder = savedInstanceState.getSerializable("cropCacheFolder") as File?
        takeImageFile = savedInstanceState.getSerializable("takeImageFile") as File?
        imageLoader = savedInstanceState.getSerializable("imageLoader") as ImageLoader?
        style = savedInstanceState.getSerializable("style") as CropImageView.Style
        isMultiMode = savedInstanceState.getBoolean("multiMode")
        isCrop = savedInstanceState.getBoolean("crop")
        isShowCamera = savedInstanceState.getBoolean("showCamera")
        isSaveRectangle = savedInstanceState.getBoolean("isSaveRectangle")
        selectLimit = savedInstanceState.getInt("selectLimit")
        outPutX = savedInstanceState.getInt("outPutX")
        outPutY = savedInstanceState.getInt("outPutY")
        focusWidth = savedInstanceState.getInt("focusWidth")
        focusHeight = savedInstanceState.getInt("focusHeight")
    }

    /**
     * 用于手机内存不足，进程被系统回收时的状态保存
     */
    fun saveInstanceState(outState: Bundle) {
        outState.putSerializable("cropCacheFolder", cropCacheFolder)
        outState.putSerializable("takeImageFile", takeImageFile)
        outState.putSerializable("imageLoader", imageLoader)
        outState.putSerializable("style", style)
        outState.putBoolean("multiMode", isMultiMode)
        outState.putBoolean("crop", isCrop)
        outState.putBoolean("showCamera", isShowCamera)
        outState.putBoolean("isSaveRectangle", isSaveRectangle)
        outState.putInt("selectLimit", selectLimit)
        outState.putInt("outPutX", outPutX)
        outState.putInt("outPutY", outPutY)
        outState.putInt("focusWidth", focusWidth)
        outState.putInt("focusHeight", focusHeight)
    }

    /**
     * 获取选中图片所在集合中的索引.
     */
    fun imageIndexOf(imageItem: ImageItem): Int {
        return mSelectedImages!!.indexOf(imageItem)
    }

//    companion object {
//        private var mInstance: ImagePicker? = null
//        @JvmStatic
//        val instance: ImagePicker?
//            get() {
//                if (mInstance == null) {
//                    synchronized(ImagePicker::class.java) {
//                        if (mInstance == null) {
//                            mInstance = ImagePicker()
//                        }
//                    }
//                }
//                return mInstance
//            }
//    }

    /**
     * 扫描图片
     */
    fun galleryAddPic(context: Context, file: File?) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val contentUri = Uri.fromFile(file)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    /**
     * 根据系统时间、前缀、后缀产生一个文件
     */
    fun createFile(folder: File?, prefix: String, suffix: String): File {
        if (!folder!!.exists() || !folder.isDirectory) folder.mkdirs()
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA)
        val filename = prefix + dateFormat.format(Date(System.currentTimeMillis())) + suffix
        return File(folder, filename)
    }

}