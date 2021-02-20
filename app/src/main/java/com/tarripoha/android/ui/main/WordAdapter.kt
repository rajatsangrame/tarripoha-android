package com.tarripoha.android.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.util.ItemClickListener
import kotlinx.android.synthetic.main.layout_adapter_word.view.name_tv

class WordAdapter(
  private var words: MutableList<Word>,
  private val itemClickListener: ItemClickListener<Word>
) : RecyclerView.Adapter<WordAdapter.ViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): ViewHolder {
    val view = LayoutInflater
        .from(parent.context)
        .inflate(R.layout.layout_adapter_word, parent, false)
    return ViewHolder(view)
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

  inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val nameTv: TextView = itemView.name_tv
    private lateinit var word: Word

    init {
      itemView.setOnClickListener {
        itemClickListener.onClick(adapterPosition, words[adapterPosition])
      }
    }

    fun getTea(): Word = word
  }

}
