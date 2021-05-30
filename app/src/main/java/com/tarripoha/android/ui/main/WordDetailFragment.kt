package com.tarripoha.android.ui.main

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList.Config.Builder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState.LOADED
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentWordDetailBinding
import com.tarripoha.android.paging.CommentPagingAdapter
import com.tarripoha.android.paging.CommentPagingAdapter.ClickMode
import com.tarripoha.android.paging.CommentPagingAdapter.OnCommentClickListener
import com.tarripoha.android.ui.OptionsBottomFragment
import com.tarripoha.android.ui.OptionsBottomFragment.Option
import com.tarripoha.android.ui.OptionsBottomFragment.OptionCLickListener
import com.tarripoha.android.ui.main.MainViewModel.FetchMode
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.helper.UserHelper
import com.tarripoha.android.util.setTextWithVisibility
import com.tarripoha.android.util.showDialog
import com.tarripoha.android.util.toJsonString
import com.tarripoha.android.util.toggleVisibility

class WordDetailFragment : Fragment() {

    // region Variables

    companion object {
        private const val TAG = "WordDetailFragment"
    }

    private lateinit var factory: ViewModelProvider.Factory
    private lateinit var binding: FragmentWordDetailBinding
    private lateinit var commentAdapter: CommentPagingAdapter
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
        menu.apply {
            findItem(R.id.menu_more).isVisible = true
            findItem(R.id.menu_search).isVisible = false
            findItem(R.id.menu_info).isVisible = false
        }
        super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_more) {
            showWordMenu()
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
        if (!isWordDetailSet()) return

        setupUI()
    }

    override fun onDestroy() {
        viewModel.apply {
            setWordDetail(null)
            setRefreshComment(null)
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
    }

    private fun isWordDetailSet(): Boolean {
        if (viewModel.getWordDetail().value == null) {
            viewModel.setUserMessage(getString(R.string.error_unknown))
            return false
        }
        return true
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
        binding.commentRv.apply {
            layoutManager = linearLayoutManager
        }
        if (!isWordDetailSet()) return
        val word = viewModel.getWordDetail().value!!
        setupAdapter(getOption(word))
    }

    private fun getOption(word: Word): FirestorePagingOptions<Comment> {
        // The "base query" is a query with no startAt/endAt/limit clauses that the adapter can use
        // to form smaller queries for each page.  It should only include where() and orderBy() clauses
        val baseQuery = if (viewModel.getFetchMode() == FetchMode.Recent) {
            Firebase.firestore
                .collection("comment")
                .whereEqualTo("word", word.name)
                .whereEqualTo("dirty", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
        } else {
            Firebase.firestore
                .collection("comment")
                .whereEqualTo("word", word.name)
                .whereEqualTo("dirty", false)
                .orderBy("popular", Query.Direction.ASCENDING)
        }
        val config = Builder()
            .setEnablePlaceholders(false)
            .setPrefetchDistance(4)
            .setPageSize(10)
            .build()

        return FirestorePagingOptions.Builder<Comment>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(
                baseQuery, config
            ) {
                val comment = it.toObject(Comment::class.java)!!
                comment.localStatus = false
                comment
            }
            .build()
    }

    private fun setupAdapter(options: FirestorePagingOptions<Comment>) {

        commentAdapter = CommentPagingAdapter(options = options, object : OnCommentClickListener {
            override fun onClick(
                comment: Comment,
                clickMode: ClickMode,
                position: Int
            ) {
                when (clickMode) {
                    ClickMode.LongCLick -> showCommentMenu(comment)
                    ClickMode.LikeButton -> {
                        if (!UserHelper.isLoggedIn()) {
                            viewModel.setUserMessage(getString(R.string.error_login))
                        }
                        viewModel.likeComment(comment) {
                            comment.likes = it
                            commentAdapter.notifyItemChanged(position)
                            //commentAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }) { state ->
            Log.i(TAG, "setupAdapter: $state")
            if (state == LOADED) {
                if (commentAdapter.itemCount != 0) {
                    binding.noCommentLayout.visibility = View.GONE
                } else {
                    binding.noCommentLayout.visibility = View.VISIBLE
                }
            }
        }

        binding.commentRv.adapter = commentAdapter
    }

    private fun setupEditText() {
        if (!isWordDetailSet()) return
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
        viewModel.getRefreshComment()
            .observe(viewLifecycleOwner) {
                it?.let {
                    commentAdapter.refresh()
                }
            }
    }

    private fun setupUi(word: Word) {
        binding.apply {
            wordTv.text = word.name
            meaningTv.text = word.meaning
            engMeaningTv.setTextWithVisibility(word.eng)
            noCommentLayout.toggleVisibility(list = word.comments)
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
        val user = UserHelper.getUser()
        if (user == null) {
            viewModel.setUserMessage(getString(R.string.error_login))
            return false
        }
        if (!isWordDetailSet()) return false
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
                        .toDouble(),
                    userId = user.phone,
                    userName = if (user.name.isNotEmpty()) user.name else getString(R.string.user)
                )
            )
            binding.commentEt.text = null
        }
        return validate
    }

    private fun showCommentMenu(comment: Comment) {
        val option = getCommentOptions(comment)
        val bundle = Bundle()
        bundle.putString(OptionsBottomFragment.KEY_OPTIONS, option.toJsonString())
        val bottomSheet = OptionsBottomFragment.newInstance(
            bundle = bundle,
            callback = object : OptionCLickListener {
                override fun onClick(option: Option) {
                    Log.d(TAG, "onClick: $option} ${comment.comment}")
                    when (option) {
                        Option.Delete -> {
                            MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                                .showDialog(
                                    message = getString(R.string.msg_confirm_delete),
                                    positiveText = getString(R.string.delete),
                                    positiveListener = {
                                        viewModel.deleteComment(comment = comment)
                                    }
                                )
                        }
                    }
                }
            }
        )
        bottomSheet.show(parentFragmentManager, OptionsBottomFragment.TAG)
    }

    private fun getCommentOptions(comment: Comment): List<Option> {
        val options = mutableListOf<Option>()
        options.add(Option.Copy)
        options.add(Option.Share)
        options.add(Option.Report)
        val user = UserHelper.getUser()
        val isAdmin = viewModel.isUserAdmin()
        if (isAdmin) {
            options.add(Option.Edit)
        }
        user?.let {
            if (comment.userId == it.phone || isAdmin) {
                options.add(Option.Delete)
            }
        }
        return options
    }

    private fun showWordMenu() {
        val option = getWordOptions()
        val bundle = Bundle()
        bundle.putString(OptionsBottomFragment.KEY_OPTIONS, option.toJsonString())
        val bottomSheet = OptionsBottomFragment.newInstance(
            bundle = bundle,
            callback = object : OptionCLickListener {
                override fun onClick(option: Option) {
                    Log.d(TAG, "onClick: $option}")
                    // no-op
                }
            }
        )
        bottomSheet.show(parentFragmentManager, OptionsBottomFragment.TAG)
    }

    private fun getWordOptions(): List<Option> {
        val options = mutableListOf<Option>()
        options.add(Option.Copy)
        options.add(Option.Share)
        options.add(Option.Report)
        val isAdmin = viewModel.isUserAdmin()
        if (isAdmin) {
            options.add(Option.Edit)
            options.add(Option.Delete)
        }
        return options
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
