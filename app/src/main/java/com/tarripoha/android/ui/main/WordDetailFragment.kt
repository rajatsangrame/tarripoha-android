package com.tarripoha.android.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.TPApp
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentWordDetailBinding
import com.tarripoha.android.ui.OptionsBottomFragment
import com.tarripoha.android.ui.OptionsBottomFragment.Option
import com.tarripoha.android.ui.OptionsBottomFragment.OptionCLickListener
import com.tarripoha.android.util.ItemLongClickListener
import com.tarripoha.android.util.TPUtils

class WordDetailFragment : Fragment() {

  // region Variables

  companion object {
    private const val TAG = "WordDetailFragment"
  }

  private lateinit var factory: ViewModelProvider.Factory
  private lateinit var binding: FragmentWordDetailBinding
  private lateinit var commentAdapter: CommentAdapter
  private val viewModel by activityViewModels<MainViewModel> {
    factory
  }

  // endregion

  // region Fragment Related Methods

  override fun onCreate(savedInstanceState: Bundle?) {
    setHasOptionsMenu(true)
    super.onCreate(savedInstanceState)
  }

  override fun onPrepareOptionsMenu(menu: Menu) {
    menu.clear()
    super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == R.id.menu_more) {
      return true
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentWordDetailBinding
        .inflate(LayoutInflater.from(requireContext()), container, false)
    return binding.root
  }

  /**
   * Called when fragment's activity is created.
   * 1. Setup UI for the activity. See [setupUI].
   *
   * @param savedInstanceState Saved data on config or state change.
   */
  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    factory =
      ViewModelProvider.AndroidViewModelFactory(TPApp.get(requireContext()))
    if (viewModel.getWordDetail().value == null) {
      viewModel.setUserMessage(getString(R.string.error_unknown))
      return
    }

    setupUI()
  }

  override fun onDestroy() {
    viewModel.apply {
      setWordDetail(null)
      setPostComment(null)
    }
    super.onDestroy()
  }

  // endregion

  // region Helper Methods

  private fun setupUI() {
    setupRecyclerView()
    setupListeners()
    setupObservers()
    setupEditText()
    checkPostBtnColor("")
    viewModel.fetchComment()
  }

  private fun checkPostBtnColor(query: String) {
    binding.postCommentBtn.apply {
      isEnabled = if (query.isNotEmpty()) {
        setImageResource(R.drawable.ic_send_black)
        true
      } else {
        setImageResource(R.drawable.ic_send_grey)
        false
      }
    }
  }

  private fun setupRecyclerView() {
    val linearLayoutManager = LinearLayoutManager(
        context, RecyclerView.VERTICAL, false
    )
    linearLayoutManager.reverseLayout = true
    commentAdapter = CommentAdapter(ArrayList(), object : ItemLongClickListener<Comment> {
      override fun onClick(
        position: Int,
        data: Comment
      ) {
        showOptionMenu(data)
      }
    })
    binding.commentRv.apply {
      layoutManager = linearLayoutManager
      adapter = commentAdapter
      registerForContextMenu(this)
    }
  }

  private fun setupEditText() {
    if (viewModel.getWordDetail().value == null) {
      viewModel.setUserMessage(getString(R.string.error_unknown))
      return
    }
    val word = viewModel.getWordDetail().value!!
    binding.commentEt.apply {
      hint = getString(R.string.add_quotes, word.name)
      setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
          val result = postComment()
          // Prevent keyboard to hide when result failed
          return@setOnEditorActionListener !result
        }
        false
      }
      doAfterTextChanged {
        it?.let { editable ->
          checkPostBtnColor(editable.toString())
        }
      }
    }
  }

  private fun setupObservers() {
    viewModel.getWordDetail()
        .observe(viewLifecycleOwner) {
          it?.let { word ->
            setupUi(word)
          }
        }
    viewModel.getPostComment()
        .observe(viewLifecycleOwner) {
          it?.let {
            //commentAdapter.addComment(it)
          }
        }
  }

  private fun setupUi(word: Word) {
    binding.apply {
      wordTv.text = word.name
      meaningTv.text = word.meaning
      if (!word.eng.isNullOrEmpty()) {
        engMeaningTv.text = word.eng
        engMeaningTv.visibility = View.VISIBLE
      }
      if (word.comments.isNullOrEmpty()) {
        noCommentLayout.visibility = View.VISIBLE
      } else {
        noCommentLayout.visibility = View.GONE
      }
      word.comments?.let { comments ->
        commentAdapter.setComments(comments)
      }
    }
  }

  private fun validateComment(): Boolean {
    val isEmpty = binding.commentEt.text
        .trim()
        .isEmpty()
    if (isEmpty) viewModel.setUserMessage(getString(R.string.empty_field))
    return !isEmpty
  }

  private fun postComment(): Boolean {
    if (viewModel.getWordDetail().value == null) {
      viewModel.setUserMessage(getString(R.string.error_unknown))
      return false
    }
    val validate = validateComment()
    if (validate) {
      val comment = binding.commentEt.text.trim()
          .toString()
      val word = viewModel.getWordDetail().value!!
      viewModel.postComment(
          Comment(
              id = TPUtils.getRandomUuid(),
              word = word.name,
              comment = comment,
              timestamp = System.currentTimeMillis()
          )
      )
      binding.commentEt.text = null
    }
    return validate
  }

  private fun showOptionMenu(comment: Comment) {
    val bottomSheet = OptionsBottomFragment.newInstance(
        callback = object : OptionCLickListener {
          override fun onClick(option: Option) {
            Log.d(TAG, "onClick: $option} ${comment.comment}")
            // no-op
          }
        },
        bundle = Bundle()
    )
    bottomSheet.show(parentFragmentManager, OptionsBottomFragment.TAG)
  }

  // endregion

  // region Click Related Methods

  private fun setupListeners() {
    binding.postCommentBtn.setOnClickListener {
      postComment()
    }
  }

  // endregion
}
