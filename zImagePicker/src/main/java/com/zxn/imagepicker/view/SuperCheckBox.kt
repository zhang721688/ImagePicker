package com.zxn.imagepicker.view

import android.content.Context
import android.util.AttributeSet
import android.view.SoundEffectConstants
import androidx.appcompat.widget.AppCompatCheckBox

/**
 * 图片选择的右侧选择勾选框.
 */
class SuperCheckBox : AppCompatCheckBox {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun performClick(): Boolean {
        val handled = super.performClick()
        if (!handled) {
            //只有当点击监听器被调用时，视图才会产生声音效果，所以我们需要在这里做一个
            playSoundEffect(SoundEffectConstants.CLICK)
        }
        return handled
    }
}