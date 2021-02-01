package com.lzy.imagepickerdemo

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

/**
 * 选择对话框
 *
 */
class SelectDialog : Dialog, View.OnClickListener, AdapterView.OnItemClickListener {

    private var mListener: SelectDialogListener? = null
    private var mActivity: Activity
    private var mMBtn_Cancel: Button? = null
    private var mTv_Title: TextView? = null
    private lateinit var mName: List<String>
    private var mTitle: String? = null
    private var mUseCustomColor = false
    private var mFirstItemColor = 0
    private var mOtherItemColor = 0

    interface SelectDialogListener {
        fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long)
    }

    /**
     * 取消事件监听接口
     *
     */
    private var mCancelListener: SelectDialogCancelListener? = null

    interface SelectDialogCancelListener {
        fun onCancelClick(v: View?)
    }

    constructor(activity: Activity, theme: Int,
                listener: SelectDialogListener, names: List<String>) : super(activity, theme) {
        mActivity = activity
        mListener = listener
        mName = names
        setCanceledOnTouchOutside(true)
    }

    /**
     * @param activity 调用弹出菜单的activity
     * @param theme 主题
     * @param listener 菜单项单击事件
     * @param cancelListener 取消事件
     * @param names 菜单项名称
     */
    constructor(activity: Activity, theme: Int, listener: SelectDialogListener, cancelListener: SelectDialogCancelListener?, names: List<String>) : super(activity, theme) {
        mActivity = activity
        mListener = listener
        mCancelListener = cancelListener
        mName = names

        // 设置是否点击外围不解散
        setCanceledOnTouchOutside(false)
    }

    /**
     * @param activity 调用弹出菜单的activity
     * @param theme 主题
     * @param listener 菜单项单击事件
     * @param names 菜单项名称
     * @param title 菜单标题文字
     */
    constructor(activity: Activity, theme: Int, listener: SelectDialogListener, names: List<String>, title: String?) : super(activity, theme) {
        mActivity = activity
        mListener = listener
        mName = names
        mTitle = title

        // 设置是否点击外围可解散
        setCanceledOnTouchOutside(true)
    }

    constructor(activity: Activity, theme: Int, listener: SelectDialogListener, cancelListener: SelectDialogCancelListener?, names: List<String>, title: String?) : super(activity, theme) {
        mActivity = activity
        mListener = listener
        mCancelListener = cancelListener
        mName = names
        mTitle = title

        // 设置是否点击外围可解散
        setCanceledOnTouchOutside(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val view = layoutInflater.inflate(R.layout.view_dialog_select,
                null)
        setContentView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT))


         //设置显示动画
        window?.run {
            setWindowAnimations(R.style.main_menu_animstyle)
            val wl = attributes
            wl.x = 0
            wl.y = mActivity.windowManager.defaultDisplay.height
            // 以下这两句是为了保证按钮可以水平满屏
            wl.width = ViewGroup.LayoutParams.MATCH_PARENT
            wl.height = ViewGroup.LayoutParams.WRAP_CONTENT
            // 设置显示位置
            onWindowAttributesChanged(wl)
        }

        initViews()
    }

    private fun initViews() {
        val dialogAdapter = DialogAdapter(mName)
        val dialogList = findViewById<ListView>(R.id.dialog_list)
        dialogList.onItemClickListener = this
        dialogList.adapter = dialogAdapter
        mMBtn_Cancel = findViewById<View>(R.id.mBtn_Cancel) as Button
        mTv_Title = findViewById<View>(R.id.mTv_Title) as TextView
        mMBtn_Cancel!!.setOnClickListener { v ->
            if (mCancelListener != null) {
                mCancelListener!!.onCancelClick(v)
            }
            dismiss()
        }
        if (!TextUtils.isEmpty(mTitle) && mTv_Title != null) {
            mTv_Title!!.visibility = View.VISIBLE
            mTv_Title!!.text = mTitle
        } else {
            mTv_Title!!.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        dismiss()
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int,
                             id: Long) {
        mListener?.onItemClick(parent, view, position, id)
        dismiss()
    }

    private inner class DialogAdapter(private val mStrings: List<String>) : BaseAdapter() {
        private var viewholder: Viewholder? = null
        private val layoutInflater: LayoutInflater
        override fun getCount(): Int {

            return mStrings.size
        }

        override fun getItem(position: Int): Any {
            return mStrings[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView
            if (null == convertView) {
                viewholder = Viewholder()
                convertView = layoutInflater.inflate(R.layout.view_dialog_item, null)
                viewholder!!.dialogItemButton = convertView.findViewById<View>(R.id.dialog_item_bt) as TextView
                convertView.tag = viewholder
            } else {
                viewholder = convertView.tag as Viewholder
            }
            viewholder!!.dialogItemButton!!.text = mStrings[position]
            if (!mUseCustomColor) {
                mFirstItemColor = mActivity.resources.getColor(R.color.blue)
                mOtherItemColor = mActivity.resources.getColor(R.color.blue)
            }
            if (1 == mStrings.size) {
                viewholder!!.dialogItemButton!!.setTextColor(mFirstItemColor)
                viewholder!!.dialogItemButton!!.setBackgroundResource(R.drawable.dialog_item_bg_only)
            } else if (position == 0) {
                viewholder!!.dialogItemButton!!.setTextColor(mFirstItemColor)
                viewholder!!.dialogItemButton!!.setBackgroundResource(R.drawable.select_dialog_item_bg_top)
            } else if (position == mStrings.size - 1) {
                viewholder!!.dialogItemButton!!.setTextColor(mOtherItemColor)
                viewholder!!.dialogItemButton!!.setBackgroundResource(R.drawable.select_dialog_item_bg_buttom)
            } else {
                viewholder!!.dialogItemButton!!.setTextColor(mOtherItemColor)
                viewholder!!.dialogItemButton!!.setBackgroundResource(R.drawable.select_dialog_item_bg_center)
            }
            return convertView!!
        }

        init {
            layoutInflater = mActivity.layoutInflater
        }
    }

    class Viewholder {
        var dialogItemButton: TextView? = null
    }

    /**
     * 设置列表项的文本颜色
     */
    fun setItemColor(firstItemColor: Int, otherItemColor: Int) {
        mFirstItemColor = firstItemColor
        mOtherItemColor = otherItemColor
        mUseCustomColor = true
    }
}