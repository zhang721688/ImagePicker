package com.zxn.imagepicker.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zxn.imagepicker.DataHolder;
import com.zxn.imagepicker.ImageDataSource;
import com.zxn.imagepicker.ImagePicker;
import com.zxn.imagepicker.R;
import com.zxn.imagepicker.adapter.ImageFolderAdapter;
import com.zxn.imagepicker.adapter.ImageRecyclerAdapter;
import com.zxn.imagepicker.bean.ImageFolder;
import com.zxn.imagepicker.bean.ImageItem;
import com.zxn.imagepicker.util.Utils;
import com.zxn.imagepicker.view.FolderPopUpWindow;
import com.zxn.imagepicker.view.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================
 * 参考：（ Github地址：https://github.com/jeasonlzy0216
 * 版    本：1.0
 * 创建日期：2016/5/19
 * 描    述：
 * 修订历史：Manifest.permission
 * 2017-03-17
 *
 * @author nanchen
 * 新增可直接传递是否裁剪参数，以及直接拍照
 * ================================================
 */
public class ImageGridActivity extends ImageBaseActivity implements ImageDataSource.OnImagesLoadedListener, ImageRecyclerAdapter.OnImageItemClickListener, ImagePicker.OnImageSelectedListener, View.OnClickListener {

    public static final int REQUEST_PERMISSION_STORAGE = 0x01;
    public static final int REQUEST_PERMISSION_CAMERA = 0x02;
    public static final String EXTRAS_TAKE_PICKERS = "TAKE";
    public static final String EXTRAS_IMAGES = "IMAGES";

    private ImagePicker imagePicker;

    private boolean isOrigin = false;  //是否选中原图
    private View mFooterBar;     //底部栏
    private Button mBtnOk;       //确定按钮
    private View mllDir; //文件夹切换按钮
    private TextView mtvDir; //显示当前文件夹
    private TextView mBtnPre;      //预览按钮
    private ImageFolderAdapter mImageFolderAdapter;    //图片文件夹的适配器
    private FolderPopUpWindow mFolderPopupWindow;  //ImageSet的PopupWindow
    private List<ImageFolder> mImageFolders;   //所有的图片文件夹
    //    private ImageGridAdapter mImageGridAdapter;  //图片九宫格展示的适配器
    private boolean directPhoto = false; // 默认不是直接调取相机
    private RecyclerView mRecyclerView;
    private ImageRecyclerAdapter mRecyclerAdapter;


    /**
     * 直接进入相册选取页面,并获取结果回调数据.
     *
     * @param fragment    activity
     * @param images      已经选择过照片集合.
     * @param requestCode 请求码.
     */
    public static void jumpToForResult(Fragment fragment, ArrayList<ImageItem> images, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), ImageGridActivity.class);
        intent.putExtra(ImageGridActivity.EXTRAS_IMAGES, images);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 直接进入相册选取页面,并获取结果回调数据.
     *
     * @param activity    activity
     * @param images      已经选择过照片集合.
     * @param requestCode 请求码.
     */
    public static void jumpToForResult(Activity activity, ArrayList<ImageItem> images, int requestCode) {
        Intent intent = new Intent(activity, ImageGridActivity.class);
        intent.putExtra(ImageGridActivity.EXTRAS_IMAGES, images);
        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * 直接进入相册选取页面,并获取结果回调数据.
     *
     * @param activity
     */
    public static void jumpToForResult(Activity activity, int requestCode) {
        Intent intent = new Intent(activity, ImageGridActivity.class);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void jumpToForResult(Fragment fragment, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), ImageGridActivity.class);
        fragment.startActivityForResult(intent, requestCode);
    }


    /**
     * 进入相册选取页面,并获取结果回调数据.
     *
     * @param activity    activity
     * @param directPhoto true:直接打开相机,false:不打开相机进入相册.默认直接相册.
     * @param requestCode
     */
    public static void jumpToForResult(Activity activity, boolean directPhoto, int requestCode) {
        Intent intent = new Intent(activity, ImageGridActivity.class);
        if (directPhoto) {
            intent.putExtra(EXTRAS_TAKE_PICKERS, directPhoto);
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public static void jumpToForResult(Fragment fragment, boolean directPhoto, int requestCode) {
        Intent intent = new Intent(fragment.getContext(), ImageGridActivity.class);
        if (directPhoto) {
            intent.putExtra(EXTRAS_TAKE_PICKERS, directPhoto);
        }
        fragment.startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        directPhoto = savedInstanceState.getBoolean(EXTRAS_TAKE_PICKERS, false);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRAS_TAKE_PICKERS, directPhoto);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);

        imagePicker = ImagePicker.getInstance();
        imagePicker.clear();
        imagePicker.addOnImageSelectedListener(this);

        Intent data = getIntent();
        // 新增可直接拍照
        if (data != null && data.getExtras() != null) {
            directPhoto = data.getBooleanExtra(EXTRAS_TAKE_PICKERS, false); // 默认不是直接打开相机
            if (directPhoto) {
                imagePicker.takePicture(this, ImagePicker.REQUEST_CODE_TAKE);
            }
            ArrayList<ImageItem> images = (ArrayList<ImageItem>) data.getSerializableExtra(EXTRAS_IMAGES);
            imagePicker.setSelectedImages(images);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

        findViewById(R.id.btn_back).setOnClickListener(this);
        mBtnOk = (Button) findViewById(R.id.btn_ok);
        mBtnOk.setOnClickListener(this);
        mBtnPre = (TextView) findViewById(R.id.btn_preview);
        mBtnPre.setOnClickListener(this);
        mFooterBar = findViewById(R.id.footer_bar);
        mllDir = findViewById(R.id.ll_dir);
        mllDir.setOnClickListener(this);
        mtvDir = (TextView) findViewById(R.id.tv_dir);
        if (imagePicker.isMultiMode()) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnPre.setVisibility(View.VISIBLE);
        } else {
            mBtnOk.setVisibility(View.GONE);
            mBtnPre.setVisibility(View.GONE);
        }

//        mImageGridAdapter = new ImageGridAdapter(this, null);
        mImageFolderAdapter = new ImageFolderAdapter(this, null);
        mRecyclerAdapter = new ImageRecyclerAdapter(this, null);

        onImageSelected(0, null, false);

        new ImageDataSource(this, null, this);
    }


    @Override
    protected void onDestroy() {
        imagePicker.removeOnImageSelectedListener(this);
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_ok) {
            Intent intent = new Intent();
            intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
            setResult(ImagePicker.RESULT_CODE_ITEMS, intent);  //多选不允许裁剪裁剪，返回数据
            finish();
        } else if (id == R.id.ll_dir) {
            if (mImageFolders == null) {
                Log.i("ImageGridActivity", "您的手机没有图片");
                return;
            }
            //点击文件夹按钮
            createPopupFolderList();
            mImageFolderAdapter.refreshData(mImageFolders);  //刷新数据
            if (mFolderPopupWindow.isShowing()) {
                mFolderPopupWindow.dismiss();
            } else {
                mFolderPopupWindow.showAtLocation(mFooterBar, Gravity.NO_GRAVITY, 0, 0);
                //默认选择当前选择的上一个，当目录很多时，直接定位到已选中的条目
                int index = mImageFolderAdapter.getSelectIndex();
                index = index == 0 ? index : index - 1;
                mFolderPopupWindow.setSelection(index);
            }
        } else if (id == R.id.btn_preview) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, 0);
            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getSelectedImages());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            intent.putExtra(ImagePicker.EXTRA_FROM_ITEMS, true);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);
        } else if (id == R.id.btn_back) {
            //点击返回按钮
            finish();
        }
    }

    /**
     * 创建弹出的ListView
     */
    private void createPopupFolderList() {
        mFolderPopupWindow = new FolderPopUpWindow(this, mImageFolderAdapter);
        mFolderPopupWindow.setOnItemClickListener(new FolderPopUpWindow.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mImageFolderAdapter.setSelectIndex(position);
                imagePicker.setCurrentImageFolderPosition(position);
                mFolderPopupWindow.dismiss();
                ImageFolder imageFolder = (ImageFolder) adapterView.getAdapter().getItem(position);
                if (null != imageFolder) {
//                    mImageGridAdapter.refreshData(imageFolder.images);
                    mRecyclerAdapter.refreshData(imageFolder.images);
                    mtvDir.setText(imageFolder.name);
                }
            }
        });
        mFolderPopupWindow.setMargin(mFooterBar.getHeight());
    }

    @Override
    public void onImagesLoaded(List<ImageFolder> imageFolders) {
        this.mImageFolders = imageFolders;
        imagePicker.setImageFolders(imageFolders);
        if (imageFolders.size() == 0) {
//            mImageGridAdapter.refreshData(null);
            mRecyclerAdapter.refreshData(null);
        } else {
//            mImageGridAdapter.refreshData(imageFolders.get(0).images);
            mRecyclerAdapter.refreshData(imageFolders.get(0).images);
        }
//        mImageGridAdapter.setOnImageItemClickListener(this);
        mRecyclerAdapter.setOnImageItemClickListener(this);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, Utils.dp2px(this, 2), false));
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mImageFolderAdapter.refreshData(imageFolders);
    }

    @Override
    public void onImageItemClick(View view, ImageItem imageItem, int position) {
        //根据是否有相机按钮确定位置
        position = imagePicker.isShowCamera() ? position - 1 : position;
        if (imagePicker.isMultiMode()) {
            Intent intent = new Intent(ImageGridActivity.this, ImagePreviewActivity.class);
            intent.putExtra(ImagePicker.EXTRA_SELECTED_IMAGE_POSITION, position);

            /**
             * 2017-03-20
             *
             * 依然采用弱引用进行解决，采用单例加锁方式处理
             */

            // 据说这样会导致大量图片的时候崩溃
//            intent.putExtra(ImagePicker.EXTRA_IMAGE_ITEMS, imagePicker.getCurrentImageFolderItems());

            // 但采用弱引用会导致预览弱引用直接返回空指针
            DataHolder.getInstance().save(DataHolder.DH_CURRENT_IMAGE_FOLDER_ITEMS, imagePicker.getCurrentImageFolderItems());
            intent.putExtra(ImagePreviewActivity.ISORIGIN, isOrigin);
            startActivityForResult(intent, ImagePicker.REQUEST_CODE_PREVIEW);  //如果是多选，点击图片进入预览界面
        } else {
            imagePicker.clearSelectedImages();
            imagePicker.addSelectedImageItem(position, imagePicker.getCurrentImageFolderItems().get(position), true);
            if (imagePicker.isCrop()) {
                Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
                startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
            } else {
                Intent intent = new Intent();
                intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                finish();
            }
        }
    }

    @SuppressLint("StringFormatMatches")
    @Override
    public void onImageSelected(int position, ImageItem item, boolean isAdd) {
        if (imagePicker.getSelectImageCount() > 0) {
            mBtnOk.setText(getString(R.string.ip_select_complete, imagePicker.getSelectImageCount(), imagePicker.getSelectLimit()));
            mBtnOk.setEnabled(true);
            mBtnPre.setEnabled(true);
            mBtnPre.setText(getResources().getString(R.string.ip_preview_count, imagePicker.getSelectImageCount()));
            mBtnPre.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted));
            mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.ip_text_primary_inverted));
        } else {
            mBtnOk.setText(getString(R.string.ip_complete));
            mBtnOk.setEnabled(false);
            mBtnPre.setEnabled(false);
            mBtnPre.setText(getResources().getString(R.string.ip_preview));
            mBtnPre.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted));
            mBtnOk.setTextColor(ContextCompat.getColor(this, R.color.ip_text_secondary_inverted));
        }
//        mImageGridAdapter.notifyDataSetChanged();
//        mRecyclerAdapter.notifyItemChanged(position); // 17/4/21 fix the position while click img to preview
//        mRecyclerAdapter.notifyItemChanged(position + (imagePicker.isShowCamera() ? 1 : 0));// 17/4/24  fix the position while click right bottom preview button
        for (int i = imagePicker.isShowCamera() ? 1 : 0; i < mRecyclerAdapter.getItemCount(); i++) {
            if (mRecyclerAdapter.getItem(i).path != null && mRecyclerAdapter.getItem(i).path.equals(item.path)) {
                mRecyclerAdapter.notifyItemChanged(i);
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && data.getExtras() != null) {
            if (resultCode == ImagePicker.RESULT_CODE_BACK) {
                isOrigin = data.getBooleanExtra(ImagePreviewActivity.ISORIGIN, false);
            } else {
                //从拍照界面返回
                //点击 X , 没有选择照片
                if (data.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS) == null) {
                    //什么都不做 直接调起相机
                } else {
                    //说明是从裁剪页面过来的数据，直接返回就可以
                    setResult(ImagePicker.RESULT_CODE_ITEMS, data);
                }
                finish();
            }
        } else {
            //如果是裁剪，因为裁剪指定了存储的Uri，所以返回的data一定为null
            if (resultCode == RESULT_OK && requestCode == ImagePicker.REQUEST_CODE_TAKE) {
                //发送广播通知图片增加了
                ImagePicker.galleryAddPic(this, imagePicker.getTakeImageFile());

                /**
                 * 2017-03-21 对机型做旋转处理
                 */
                String path = imagePicker.getTakeImageFile().getAbsolutePath();
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

                ImageItem imageItem = new ImageItem();
                imageItem.path = path;
                imagePicker.clearSelectedImages();
                imagePicker.addSelectedImageItem(0, imageItem, true);
                if (imagePicker.isCrop()) {
                    Intent intent = new Intent(ImageGridActivity.this, ImageCropActivity.class);
                    startActivityForResult(intent, ImagePicker.REQUEST_CODE_CROP);  //单选需要裁剪，进入裁剪界面
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(ImagePicker.EXTRA_RESULT_ITEMS, imagePicker.getSelectedImages());
                    setResult(ImagePicker.RESULT_CODE_ITEMS, intent);   //单选不需要裁剪，返回数据
                    finish();
                }
            } else if (directPhoto) {
                finish();
            }
        }
    }

}