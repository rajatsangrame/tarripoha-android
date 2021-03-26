package com.tarripoha.android.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.ui.BaseViewHolder
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.databinding.LayoutItemCommentBinding

class CommentAdapter(
  private var comments: MutableList<Comment>,
  private val itemClickListener: ItemClickListener<Comment>
) : RecyclerView.Adapter<BaseViewHolder>() {

  override fun onCreateViewHolder(
    parent: ViewGroup,
    viewType: Int
  ): BaseViewHolder {

    val binding = LayoutItemCommentBinding
      .inflate(LayoutInflater.from(parent.context), parent, false)
    return CommentViewHolder(binding)

  }

  override fun onBindViewHolder(
    holder: BaseViewHolder,
    position: Int
  ) {
    holder.bind(position)
  }

  override fun getItemCount(): Int = comments.size

  fun setComments(comments: List<Comment>) {
    this.comments.clear()
    this.comments.addAll(comments)

    notifyDataSetChanged()
  }

  inner class CommentViewHolder(binding: LayoutItemCommentBinding) : BaseViewHolder(
    binding.root
  ) {

    private val userTv: TextView = binding.userTv
    private val commentTv: TextView = binding.commentTv

    init {
      itemView.setOnClickListener {
        itemClickListener.onClick(adapterPosition, comments[adapterPosition])
      }
    }

    override fun bind(position: Int) {
      val comment = comments[position]
      userTv.text = comment.addedBy ?: itemView.context.getString(R.string.username)
      commentTv.text = comment.comment
    }
  }

  companion object {
    const val VIEW_TYPE_NEW_WORD = 101
  }

}