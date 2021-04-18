package com.tarripoha.android.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.databinding.LayoutItemCommentBinding
import com.tarripoha.android.ui.BaseViewHolder
import com.tarripoha.android.util.ItemLongClickListener
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.setTextWithVisibility
import java.lang.Exception

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
    return CommentViewHolder(binding, itemClickListener)

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

  fun addComments(comments: List<Comment>) {
    this.comments.addAll(comments)
    notifyDataSetChanged()
  }

  class CommentViewHolder(
    private val binding: LayoutItemCommentBinding,
    private val itemClickListener: ItemLongClickListener<Comment>
  ) : BaseViewHolder(
    binding.root
  ) {

    override fun bind(position: Int) {
      //no-op
    }

    override fun bind(data: Any) {
      if (data is Comment) {
        val user = data.userName ?: itemView.context.getString(R.string.user)


        binding.apply {
          userTv.text = user
          commentTv.text = data.comment
          avatarTv.text = user[0].toString()

          val time = TPUtils.getTime(itemView.context, data.timestamp)
          timestampTv.setTextWithVisibility(time)
          root.setOnLongClickListener {
            itemClickListener.onClick(position = adapterPosition, data = data)
            return@setOnLongClickListener true
          }
        }
      }
    }
  }

  fun getLastTimeStamp(): Double {
    return try {
      comments[comments.size - 1].timestamp
    } catch (e: Exception) {
      return 0.0
    }
  }

  fun getLastPopularity(): Double {
    return try {
      comments[comments.size - 1].popular
    } catch (e: Exception) {
      return 0.0
    }
  }

  companion object {
  }

}
