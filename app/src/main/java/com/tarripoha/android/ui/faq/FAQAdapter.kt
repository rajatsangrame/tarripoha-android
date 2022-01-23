package com.tarripoha.android.ui.faq

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.R
import com.tarripoha.android.data.model.FAQ
import com.tarripoha.android.databinding.LayoutItemFaqBinding

internal class FAQAdapter :
    RecyclerView.Adapter<FAQAdapter.FaqVH>() {
    private var faqList = ArrayList<FAQ>()

    fun setFAQList(list: List<FAQ>) {
        faqList.clear()
        faqList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FAQAdapter.FaqVH {
        val binding = LayoutItemFaqBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return FaqVH(binding)
    }

    inner class FaqVH(val binding: LayoutItemFaqBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val faq = faqList[adapterPosition]
                faq.localExpandedState = !faq.localExpandedState
                notifyDataSetChanged()
            }
        }

        fun setData(item: FAQ) {
            val context = itemView.context
            binding.tvTitle.text = item.title
            binding.tvDesc.text = item.desc
            if (item.localExpandedState) {
                binding.btnExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_expand_less
                    )
                )
                binding.tvDesc.visibility = View.VISIBLE
            } else {
                binding.btnExpand.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.ic_expand_more
                    )
                )
                binding.tvDesc.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int {
        return faqList.size
    }

    override fun onBindViewHolder(holder: FaqVH, position: Int) {
        holder.setData(faqList[position])
    }
}
