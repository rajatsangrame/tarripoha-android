package com.tarripoha.android.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.LayoutAdapterWordBinding
import com.tarripoha.android.util.ItemClickListener

class WordAdapter(
  private var words: MutableList<Word>,
  private val itemClickListener: ItemClickListener<Word>
) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val binding: LayoutAdapterWordBinding = LayoutAdapterWordBinding.inflate(
        LayoutInflater
            .from(parent.context)
    )
    return ViewHolder(binding)
  }

  override fun onBindViewHolder(
    holder: ViewHolder,
    position: Int
  ) {
    val word = words[position]
    holder.nameTv.text = word.name
  }

  override fun getItemCount(): Int = words.size

  fun setWordList(words: List<Word>) {
    this.words.clear()
    this.words.addAll(words)

    notifyDataSetChanged()
  }

  inner class ViewHolder(binding: LayoutAdapterWordBinding) : RecyclerView.ViewHolder(
      binding.root
  ) {
    val nameTv: TextView = binding.nameTv
    private lateinit var word: Word

    init {
      itemView.setOnClickListener {
        itemClickListener.onClick(adapterPosition, words[adapterPosition])
      }
    }

    fun getTea(): Word = word
  }

}
