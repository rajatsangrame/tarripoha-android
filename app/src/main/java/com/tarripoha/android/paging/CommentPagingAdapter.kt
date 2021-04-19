package com.tarripoha.android.paging

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.databinding.LayoutItemCommentBinding
import com.tarripoha.android.paging.CommentPagingAdapter.CommentViewHolder
import com.tarripoha.android.ui.BaseViewHolder
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.setTextWithVisibility

/**
 * Ref:
 * https://medium.com/firebase-developers/update-queries-without-changing-recyclerview-adapter-using-firebaseui-android-32098b3082b2
 */
class CommentPagingAdapter(
  options: FirestorePagingOptions<Comment>,
  private val commentClickListener: OnCommentClickListener,
  val loadingStateChanged: (state: LoadingState) -> Unit
) :
  FirestorePagingAdapter<Comment, CommentViewHolder>(options) {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
    val binding = LayoutItemCommentBinding
      .inflate(LayoutInflater.from(parent.context), parent, false)
    return CommentViewHolder(binding = binding, commentClickListener = commentClickListener)
  }

  override fun onBindViewHolder(holder: CommentViewHolder, position: Int, model: Comment) {
    holder.bind(model)
  }

  override fun onLoadingStateChanged(state: LoadingState) {
    super.onLoadingStateChanged(state)
    loadingStateChanged(state)
  }

  class CommentViewHolder(
    private val binding: LayoutItemCommentBinding,
    private val commentClickListener: OnCommentClickListener,
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
          likeIv.setOnClickListener {
            commentClickListener.onClick(comment = data, clickMode = ClickMode.LikeButton)
          }
          root.setOnLongClickListener {
            commentClickListener.onClick(comment = data, clickMode = ClickMode.LongCLick)
            return@setOnLongClickListener true
          }
        }
      }
    }
  }

  interface OnCommentClickListener {
    fun onClick(comment: Comment, clickMode: ClickMode)
  }

  sealed class ClickMode {
    object LongCLick : ClickMode()
    object LikeButton : ClickMode()
  }
}

