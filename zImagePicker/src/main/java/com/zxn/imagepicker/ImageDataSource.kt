package com.zxn.imagepicker

import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.FragmentActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.zxn.imagepicker.bean.ImageFolder
import com.zxn.imagepicker.bean.ImageItem
import java.io.File
import java.util.*

/**
 * 描    述：加载手机图片实现类
 */
class ImageDataSource(private val activity: FragmentActivity, path: String?, //图片加载完成的回调接口
                      private val loadedListener: OnImagesLoadedListener) : LoaderManager.LoaderCallbacks<Cursor?> {

    private val IMAGE_PROJECTION = arrayOf( //查询图片需要的数据列
            MediaStore.Images.Media.DISPLAY_NAME,  //图片的显示名称  aaa.jpg
            MediaStore.Images.Media.DATA,  //图片的真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            MediaStore.Images.Media.SIZE,  //图片的大小，long型  132492
            MediaStore.Images.Media.WIDTH,  //图片的宽度，int型  1920
            MediaStore.Images.Media.HEIGHT,  //图片的高度，int型  1080
            MediaStore.Images.Media.MIME_TYPE,  //图片的类型     image/jpeg
            MediaStore.Images.Media.DATE_ADDED) //图片被添加的时间，long型  1450518608
    private val imageFolders = ArrayList<ImageFolder>() //所有的图片文件夹
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor?> {
        var cursorLoader: CursorLoader? = null
        //扫描所有图片
        if (id == LOADER_ALL) cursorLoader = CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, null, null, IMAGE_PROJECTION[6] + " DESC")
        //扫描某个图片文件夹
        if (id == LOADER_CATEGORY) cursorLoader = CursorLoader(activity, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, IMAGE_PROJECTION, IMAGE_PROJECTION[1] + " like '%" + args!!.getString("path") + "%'", null, IMAGE_PROJECTION[6] + " DESC")
        return cursorLoader!!
    }

    override fun onLoadFinished(loader: Loader<Cursor?>, data: Cursor?) {
        //imageFolders.clear();
        if (data != null) {
            val allImages = ArrayList<ImageItem>() //所有图片的集合,不分文件夹
            while (data.moveToNext()) {
                //查询数据
                val imageName = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]))
                val imagePath = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]))
                val file = File(imagePath)
                if (!file.exists() || file.length() <= 0) {
                    continue
                }
                val imageSize = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]))
                val imageWidth = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]))
                val imageHeight = data.getInt(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]))
                val imageMimeType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]))
                val imageAddTime = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]))
                //封装实体
                val imageItem = ImageItem()
                imageItem.name = imageName
                imageItem.path = imagePath
                imageItem.size = imageSize
                imageItem.width = imageWidth
                imageItem.height = imageHeight
                imageItem.mimeType = imageMimeType
                imageItem.addTime = imageAddTime
                allImages.add(imageItem)
                //根据父路径分类存放图片
                val imageFile = File(imagePath)
                val imageParentFile = imageFile.parentFile
                val imageFolder = ImageFolder()
                imageFolder.name = imageParentFile.name
                imageFolder.path = imageParentFile.absolutePath
                if (!imageFolders.contains(imageFolder)) {
                    val images = ArrayList<ImageItem>()
                    images.add(imageItem)
                    imageFolder.cover = imageItem
                    imageFolder.images = images
                    imageFolders.add(imageFolder)
                } else {
                    imageFolders[imageFolders.indexOf(imageFolder)].images.add(imageItem)
                }
            }
            //防止没有图片报异常
            if (data.count > 0 && allImages.size > 0) {
                //构造所有图片的集合
                val allImagesFolder = ImageFolder()
                allImagesFolder.name = activity.resources.getString(R.string.ip_all_images)
                allImagesFolder.path = "/"
                allImagesFolder.cover = allImages[0]
                allImagesFolder.images = allImages
                imageFolders.add(0, allImagesFolder) //确保第一条是所有图片
            }
        }

        //回调接口，通知图片数据准备完成
        ImagePicker.setImageFolders(imageFolders);
        loadedListener.onImagesLoaded(imageFolders)
    }

    override fun onLoaderReset(loader: Loader<Cursor?>) {
        println("--------")
    }

    /** 所有图片加载完成的回调接口  */
    interface OnImagesLoadedListener {
        fun onImagesLoaded(imageFolders: MutableList<ImageFolder>)
    }

    companion object {
        const val LOADER_ALL = 0 //加载所有图片
        const val LOADER_CATEGORY = 1 //分类加载图片
    }

    /**
     * @param activity       用于初始化LoaderManager，需要兼容到2.3
     * @param path           指定扫描的文件夹目录，可以为 null，表示扫描所有图片
     * @param loadedListener 图片加载完成的监听
     */
    init {
        val loaderManager = activity.supportLoaderManager
        if (path == null) {
            loaderManager.initLoader(LOADER_ALL, null, this) //加载所有的图片
        } else {
            //加载指定目录的图片
            val bundle = Bundle()
            bundle.putString("path", path)
            loaderManager.initLoader(LOADER_CATEGORY, bundle, this)
        }
    }
}