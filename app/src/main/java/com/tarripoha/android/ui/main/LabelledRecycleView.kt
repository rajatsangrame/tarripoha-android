package com.tarripoha.android.ui.main

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

    fun setOnNavigateClickListener(lister: OnClickListener) {
        binding.navigateIv.setOnClickListener(lister)
    }

    fun getRecyclerView() = binding.recycleView
}