package com.tarripoha.android.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.LayoutItemWordBinding
import com.tarripoha.android.util.ItemClickListener

class WordAdapter(
  private var words: MutableList<Word>,
  private val itemClickListener: ItemClickListener<Word>
) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val binding = LayoutItemWordBinding
        .inflate(LayoutInflater.from(parent.context), parent, false)
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    val word = words[position]
    holder.nameTv.text = word.name
    holder.meaningTv.text = word.meaning
  }

  override fun getItemCount(): Int = words.size

  fun setWordList(words: List<Word>) {
    this.words.clear()
    this.words.addAll(words)

    notifyDataSetChanged()
  }

  inner class ViewHolder(binding: LayoutItemWordBinding) : RecyclerView.ViewHolder(
      binding.root
  ) {
    val nameTv: TextView = binding.nameTv
    val meaningTv: TextView = binding.meaningTv
    private lateinit var word: Word

    init {
      itemView.setOnClickListener {
        itemClickListener.onClick(adapterPosition, words[adapterPosition])
      }
    }

    fun getTea(): Word = word
  }

}
