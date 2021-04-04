package com.tarripoha.android.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.ui.BaseViewHolder
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.databinding.LayoutItemCommentBinding
import com.tarripoha.android.util.ItemLongClickListener
import com.tarripoha.android.util.TPUtils

class CommentAdapter(
  private var comments: MutableList<Comment>,
  private val itemClickListener: ItemLongClickListener<Comment>
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

  fun addComment(comment: Comment) {
    this.comments.add(comment)
    notifyDataSetChanged()
  }

  inner class CommentViewHolder(private val binding: LayoutItemCommentBinding) : BaseViewHolder(
      binding.root
  ) {

    init {
      itemView.setOnLongClickListener {
        itemClickListener.onClick(position = adapterPosition, data = comments[adapterPosition])
        true
      }
    }

    override fun bind(position: Int) {
      val comment = comments[position]
      val user = comment.userName ?: itemView.context.getString(R.string.user)


      binding.apply {
        userTv.text = user
        commentTv.text = comment.comment
        avatarTv.text = user[0].toString()

        val time = TPUtils.getTime(itemView.context, comment.timestamp)
        TPUtils.handleViewVisibility(timestampTv, time)
      }
    }
  }

  companion object {
  }

}
