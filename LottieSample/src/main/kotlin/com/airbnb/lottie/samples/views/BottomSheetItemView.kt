package com.airbnb.lottie.samples.views

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.view.isVisible
import com.airbnb.lottie.samples.R
import com.airbnb.lottie.samples.inflate
import kotlinx.android.synthetic.main.item_view_bottom_sheet.view.*

class BottomSheetItemView(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.item_view_bottom_sheet)
    }

    @SuppressLint("SetTextI18n")
    fun set(left: String, right: String? = null) {
        leftTextView.text = left
        rightTextView.isVisible = !TextUtils.isEmpty(right)
        rightTextView.text = right
    }
}