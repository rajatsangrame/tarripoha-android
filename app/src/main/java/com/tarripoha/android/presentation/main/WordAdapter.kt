package com.tarripoha.android.presentation.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.databinding.LayoutItemWordBinding
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.databinding.LayoutNewWordPlankBinding
import com.tarripoha.android.presentation.base.BaseViewHolder
import com.tarripoha.android.R
import com.tarripoha.android.databinding.LayoutItemWordSquareBinding
import com.tarripoha.android.domain.entity.Word

class WordAdapter(
    private var words: MutableList<Word>,
    private val itemClickListener: ItemClickListener<Word>,
    private val options: ViewingOptions? = null

) : RecyclerView.Adapter<BaseViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return if (viewType == VIEW_TYPE_NEW_WORD) {
            val binding = LayoutNewWordPlankBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            NewWordPlankViewHolder(binding)
        } else if (viewType == VIEW_TYPE_SQUARE) {
            val binding = LayoutItemWordSquareBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            WordViewHolderSquare(binding)
        } else {
            val binding = LayoutItemWordBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            WordViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder,
        position: Int
    ) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = words.size

//    override fun getItemViewType(position: Int): Int {
//        val type = this.words[position].type
//        if (type != null) {
//            when (type) {
//                // Always Linear layout for new word
//                Word.TYPE_NEW_WORD -> return VIEW_TYPE_NEW_WORD
//            }
//        } else if (options?.squareView == true) return VIEW_TYPE_SQUARE
//
//        return super.getItemViewType(position)
//    }

    fun setWordList(words: List<Word>) {
        this.words.clear()
        this.words.addAll(words)

        notifyDataSetChanged()
    }

    inner class WordViewHolder(val binding: LayoutItemWordBinding) : BaseViewHolder(
        binding.root
    ) {

        init {
            itemView.setOnClickListener {
                itemClickListener.onClick(adapterPosition, words[adapterPosition])
            }
        }

        override fun bind(position: Int) {
            val context = itemView.context
            val word = words[position]
            binding.nameTv.text = word.name
            binding.meaningTv.text = word.meaning
            if (options?.showStatus == true) {
                binding.statusTv.visibility = View.VISIBLE
                when {
                    word.isDirty() -> {
                        binding.statusTv.text = context.getString(R.string.removed)
                        binding.statusTv.setBackgroundResource(R.color.colorRed)
                    }
                    !word.isApproved() && !word.isDirty() -> {
                        binding.statusTv.text = context.getString(R.string.pending)
                        binding.statusTv.setBackgroundResource(R.color.colorGrey)
                    }
                    else -> {
                        binding.statusTv.text = context.getString(R.string.approved)
                        binding.statusTv.setBackgroundResource(R.color.colorGreen)
                    }
                }
            } else binding.statusTv.visibility = View.GONE
        }

        override fun bind(data: Any) {
            // no-op
        }
    }

    inner class NewWordPlankViewHolder(private val binding: LayoutNewWordPlankBinding) :
        BaseViewHolder(
            binding.root
        ) {

        init {
            itemView.setOnClickListener {
                itemClickListener.onClick(adapterPosition, words[adapterPosition])
            }
        }

        override fun bind(position: Int) {
            val word = words[position].name
            val msg = binding.view.context.getString(R.string.msg_new_word_plank, word)
            binding.messageTv.text = msg
        }

        override fun bind(data: Any) {
            // no-op
        }
    }

    inner class WordViewHolderSquare(private val binding: LayoutItemWordSquareBinding) :
        BaseViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                itemClickListener.onClick(adapterPosition, words[adapterPosition])
            }
        }

        override fun bind(position: Int) {
            val word = words[position]
            binding.nameTv.text = word.name
            binding.meaningTv.text = word.meaning
        }

        override fun bind(data: Any) {
            // no-op
        }

    }

    data class ViewingOptions(
        var squareView: Boolean = false,
        var showStatus: Boolean = false,
        var showCheckBox: Boolean = false,
    )

    companion object {
        const val VIEW_TYPE_NEW_WORD = 101
        const val VIEW_TYPE_SQUARE = 102
    }

}
