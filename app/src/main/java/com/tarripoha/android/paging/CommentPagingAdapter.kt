package com.tarripoha.android.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.databinding.LayoutItemCommentBinding
import com.tarripoha.android.paging.CommentPagingAdapter.CommentViewHolder
import com.tarripoha.android.ui.BaseViewHolder
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.helper.UserHelper
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

    inner class CommentViewHolder(
        private val binding: LayoutItemCommentBinding,
        private val commentClickListener: OnCommentClickListener,
    ) : BaseViewHolder(
        binding.root
    ) {

        override fun bind(position: Int) {
            //no-op
        }

        override fun bind(comment: Any) {
            if (comment is Comment) {
                val user = comment.userName ?: itemView.context.getString(R.string.user)

                binding.apply {
                    userTv.text = user
                    commentTv.text = comment.comment
                    avatarTv.text = user[0].toString()
                    setLikeButton(comment, likeIv)
                    showTotalLikes(comment, totalLikesTv)
                    val time = TPUtils.getTime(itemView.context, comment.timestamp)
                    timestampTv.setTextWithVisibility(time)
                    likeIv.setOnClickListener {
                        commentClickListener.onClick(
                            comment = comment,
                            clickMode = ClickMode.LikeButton,
                            position = adapterPosition
                        )
                    }
                    root.setOnLongClickListener {
                        commentClickListener.onClick(
                            comment = comment,
                            clickMode = ClickMode.LongCLick,
                            position = adapterPosition
                        )
                        return@setOnLongClickListener true
                    }
                }
            }
        }
    }

    interface OnCommentClickListener {
        fun onClick(comment: Comment, clickMode: ClickMode, position: Int)
    }

    sealed class ClickMode {
        object LongCLick : ClickMode()
        object LikeButton : ClickMode()
    }

    /**
     * Update the tint color if user has already liked the comment
     */
    private fun setLikeButton(comment: Comment, likeIv: ImageView) {
        val likes = comment.likes
        if (likes.isNullOrEmpty()) {
            likeIv.setImageDrawable(
                ContextCompat.getDrawable(
                    likeIv.context,
                    R.drawable.ic_like_border_black
                )
            )
        } else {
            val userId = UserHelper.getPhone()
            if (likes.containsKey(userId) && likes[userId] == true) {
                // LIKE = TRUE
                likeIv.setImageDrawable(
                    ContextCompat.getDrawable(
                        likeIv.context,
                        R.drawable.ic_like_black
                    )
                )
            } else {
                likeIv.setImageDrawable(
                    ContextCompat.getDrawable(
                        likeIv.context,
                        R.drawable.ic_like_border_black
                    )
                )
            }
        }
    }

    private fun showTotalLikes(comment: Comment, view: TextView) {
        val likes = comment.likes
        if (likes.isNullOrEmpty()) {
            view.visibility = View.GONE
            return
        }
        val context = view.context
        var count = 0
        likes.forEach {
            if (it.value) {
                count++
            }
        }
        when (count) {
            0 -> {
                view.visibility = View.GONE
            }
            1 -> {
                view.text = context.getString(R.string.like, count.toString())
                view.visibility = View.VISIBLE
            }
            else -> {
                view.text = context.getString(R.string.likes, count.toString())
                view.visibility = View.VISIBLE
            }
        }
    }

}
