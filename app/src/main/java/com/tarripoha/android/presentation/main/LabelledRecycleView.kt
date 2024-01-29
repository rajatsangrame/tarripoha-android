package com.tarripoha.android.presentation.main

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutLabelledRvBinding

internal class LabelledRecycleView : LinearLayout {

    private var font = ResourcesCompat.getFont(context, R.font.montserrat_medium)

    private var binding =
        LayoutLabelledRvBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attr: AttributeSet?) : super(context, attr) {
        attr?.let {
            obtainAttrValues(attr = it)
        }
        init()
    }

    private fun init() {
        binding.labelTv.typeface = font
    }

    private fun obtainAttrValues(attr: AttributeSet) {
        //no-op
    }

    fun setLabel(label: String) {
        if (label.isNotEmpty()) binding.labelTv.text = label
    }

    fun setOptionalText(text: String) {
        if (text.isNotEmpty()) binding.optionalTv.text = text
    }

    fun setOnNavigateClickListener(lister: OnClickListener) {
        binding.navigateIv.setOnClickListener(lister)
    }

    fun setErrorView(visibility: Int, msg: String = context.getString(R.string.error_no_result)) {
        binding.errorTv.visibility = visibility
        binding.errorTv.text = msg
    }

    fun getRecyclerView() = binding.recycleView
}