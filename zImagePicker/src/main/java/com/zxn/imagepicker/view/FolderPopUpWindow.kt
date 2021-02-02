package com.zxn.imagepicker.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.*
import com.zxn.imagepicker.R

class FolderPopUpWindow(context: Context, adapter: BaseAdapter?) : PopupWindow(context), View.OnClickListener {
    private val listView: ListView
    private var onItemClickListener: OnItemClickListener? = null
    private val masker: View
    private val marginView: View
    private var marginPx = 0
    private fun enterAnimator() {
        val alpha = ObjectAnimator.ofFloat(masker, "alpha", 0f, 1f)
        val translationY = ObjectAnimator.ofFloat(listView, "translationY", listView.height.toFloat(), 0f)
        val set = AnimatorSet()
        set.duration = 400
        set.playTogether(alpha, translationY)
        set.interpolator = AccelerateDecelerateInterpolator()
        set.start()
    }

    override fun dismiss() {
        exitAnimator()
    }

    private fun exitAnimator() {
        val alpha = ObjectAnimator.ofFloat(masker, "alpha", 1f, 0f)
        val translationY = ObjectAnimator.ofFloat(listView, "translationY", 0f, listView.height.toFloat())
        val set = AnimatorSet()
        set.duration = 300
        set.playTogether(alpha, translationY)
        set.interpolator = AccelerateDecelerateInterpolator()
        set.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                listView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animator) {
                super@FolderPopUpWindow.dismiss()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        set.start()
    }

    fun setOnItemClickListener(listener: OnItemClickListener?) {
        onItemClickListener = listener
    }

    fun setSelection(selection: Int) {
        listView.setSelection(selection)
    }

    fun setMargin(marginPx: Int) {
        this.marginPx = marginPx
    }

    override fun onClick(v: View) {
        dismiss()
    }

    interface OnItemClickListener {
        fun onItemClick(adapterView: AdapterView<*>?, view: View?, position: Int, l: Long)
    }

    init {
        val view = View.inflate(context, R.layout.pop_folder_container, null)
        masker = view.findViewById(R.id.masker)
        masker.setOnClickListener(this)
        marginView = view.findViewById(R.id.margin)
        marginView.setOnClickListener(this)
        listView = view.findViewById<View>(R.id.listView) as ListView
        listView.adapter = adapter
        contentView = view
        width = ViewGroup.LayoutParams.MATCH_PARENT //如果不设置，就是 AnchorView 的宽度
        height = ViewGroup.LayoutParams.MATCH_PARENT
        isFocusable = true
        isOutsideTouchable = true
        setBackgroundDrawable(ColorDrawable(0))
        animationStyle = 0
        view.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                view.viewTreeObserver.removeGlobalOnLayoutListener(this)
                val maxHeight = view.height * 5 / 8
                val realHeight = listView.height
                val listParams = listView.layoutParams
                listParams.height = if (realHeight > maxHeight) maxHeight else realHeight
                listView.layoutParams = listParams
                val marginParams = marginView.layoutParams as LinearLayout.LayoutParams
                marginParams.height = marginPx
                marginView.layoutParams = marginParams
                enterAnimator()
            }
        })
        listView.onItemClickListener = AdapterView.OnItemClickListener { adapterView, view, position, l -> if (onItemClickListener != null) onItemClickListener!!.onItemClick(adapterView, view, position, l) }
    }
}