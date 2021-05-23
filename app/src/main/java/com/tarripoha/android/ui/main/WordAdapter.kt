package com.tarripoha.android.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.LayoutItemWordBinding
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.databinding.LayoutNewWordPlankBinding
import com.tarripoha.android.ui.BaseViewHolder
import com.tarripoha.android.R

class WordAdapter(
  private var words: MutableList<Word>,
  private val itemClickListener: ItemClickListener<Word>
) : RecyclerView.Adapter<BaseViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): BaseViewHolder {
    return if (viewType == VIEW_TYPE_NEW_WORD) {
      val binding = LayoutNewWordPlankBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
      NewWordPlankViewHolder(binding)
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

  override fun getItemViewType(position: Int): Int {
    val type = this.words[position].type
    if (type != null) {
      when (type) {
        Word.TYPE_NEW_WORD -> return VIEW_TYPE_NEW_WORD
      }
    }

    return super.getItemViewType(position)
  }

  fun setWordList(words: List<Word>) {
    this.words.clear()
    this.words.addAll(words)

    notifyDataSetChanged()
  }

  inner class WordViewHolder(binding: LayoutItemWordBinding) : BaseViewHolder(
    binding.root
  ) {

    private val nameTv: TextView = binding.nameTv
    private val meaningTv: TextView = binding.meaningTv

    init {
      itemView.setOnClickListener {
        itemClickListener.onClick(adapterPosition, words[adapterPosition])
      }
    }

    override fun bind(position: Int) {
      val word = words[position]
      nameTv.text = word.name
      meaningTv.text = word.meaning
    }

    override fun bind(data: Any) {
      // no-op
    }
  }

  inner class NewWordPlankViewHolder(private val binding: LayoutNewWordPlankBinding) :
    BaseViewHolder(
      binding.root
    ) {
    private val messageTv: TextView = binding.messageTv

    init {
      itemView.setOnClickListener {
        itemClickListener.onClick(adapterPosition, words[adapterPosition])
      }
    }

    override fun bind(position: Int) {
      val word = words[position].name
      val msg = binding.view.context.getString(R.string.msg_new_word_plank, word)
      messageTv.text = msg
    }

    override fun bind(data: Any) {
      // no-op
    }
  }

  companion object {
    const val VIEW_TYPE_NEW_WORD = 101
  }

}
