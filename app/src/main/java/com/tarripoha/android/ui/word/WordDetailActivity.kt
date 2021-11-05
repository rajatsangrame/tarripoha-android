package com.tarripoha.android.ui.word

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList.Config.Builder
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentWordDetailBinding
import com.tarripoha.android.di.component.DaggerWordDetailActivityComponent
import com.tarripoha.android.di.component.WordDetailActivityComponent
import com.tarripoha.android.paging.CommentPagingAdapter
import com.tarripoha.android.paging.CommentPagingAdapter.ClickMode
import com.tarripoha.android.paging.CommentPagingAdapter.OnCommentClickListener
import com.tarripoha.android.ui.OptionsBottomFragment
import com.tarripoha.android.ui.OptionsBottomFragment.Option
import com.tarripoha.android.ui.OptionsBottomFragment.OptionCLickListener
import com.tarripoha.android.util.helper.UserHelper
import com.tarripoha.android.ui.word.WordViewModel.FetchMode
import com.tarripoha.android.util.*
import javax.inject.Inject

class WordDetailActivity : AppCompatActivity() {

    // region Variables

    companion object {
        private const val TAG = "WordDetailFragment"
        private const val KEY_WORD_DETAIL = "word_detail"
        private const val KEY_WORD = "word"
        private const val KEY_IS_EXTERNAL_SHARE = "is_external_share"

        fun startMe(
            context: Context,
            wordDetail: Word?,
            isExternalShare: Boolean = false,
            word: String? = null
        ) {
            val intent = Intent(context, WordDetailActivity::class.java)
            intent.putExtra(KEY_WORD_DETAIL, wordDetail)
            intent.putExtra(KEY_IS_EXTERNAL_SHARE, isExternalShare)
            intent.putExtra(KEY_WORD, word)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var binding: FragmentWordDetailBinding
    private lateinit var commentAdapter: CommentPagingAdapter
    private lateinit var viewModel: WordViewModel

    private val resultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val word = result.data?.getParcelableExtra<Word>(KEY_WORD)
                if (word is Word) {
                    val user = viewModel.getPrefUser()
                    if (!word.name.isNullOrEmpty() && user?.id != null) {
                        word.updateUserRelatedData(user)
                        word.updated = System.currentTimeMillis()
                        viewModel.updateWord(word)
                    } else {
                        viewModel.setUserMessage(getString(R.string.error_unknown))
                    }
                }
            }
        }

    // endregion

    // region Activity Related Methods

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentWordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDependency()
        viewModel = ViewModelProvider(this, factory).get(WordViewModel::class.java)

        if (intent.hasExtra(KEY_IS_EXTERNAL_SHARE)) {
            val isExternalShare = intent?.getBooleanExtra(KEY_IS_EXTERNAL_SHARE, false)!!
            when {
                isExternalShare -> {
                    // handle External Share
                }
                intent.hasExtra(KEY_WORD_DETAIL) -> {
                    val word = intent?.getParcelableExtra<Word>(KEY_WORD_DETAIL)
                    viewModel.setWordDetail(word)
                    setupUI()
                }
                else -> {
                    viewModel.setUserMessage(getString(R.string.error_unknown))
                }
            }
        } else {
            viewModel.setUserMessage(getString(R.string.error_unknown))
        }
    }

    private fun getDependency() {
        val component: WordDetailActivityComponent = DaggerWordDetailActivityComponent
            .builder()
            .applicationComponent(
                TPApp.get(this)
                    .getComponent()
            )
            .build()
        component.injectActivity(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        menu.apply {
            findItem(R.id.menu_more).isVisible = true
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
                return true
            }
            R.id.menu_more -> {
                showWordMenu()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        commentAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        commentAdapter.stopListening()
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
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        setupEditText()
        checkPostBtnColor("")
    }

    private fun setupToolbar() {
        binding.toolbarLayout.title.text = null
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this@WordDetailActivity, R.color.colorPrimary))
            )
        }
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
            this, RecyclerView.VERTICAL, false
        )
        linearLayoutManager.reverseLayout = true
        binding.commentRv.apply {
            layoutManager = linearLayoutManager
        }
        if (!viewModel.isWordDetailSet()) return
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
            .setLifecycleOwner(this)
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
                        val userId = UserHelper.getPhone()
                        if (userId.isNullOrEmpty()) {
                            viewModel.setUserMessage(getString(R.string.error_login))
                            return
                        }
                        val likes: MutableMap<String, Boolean> =
                            comment.likes ?: mutableMapOf()
                        var like: Boolean
                        when {
                            likes.contains(userId) -> {
                                // Opp of likes[userId]
                                like = !likes[userId]!!
                            }
                            else -> {
                                like = true
                            }
                        }
                        viewModel.likeComment(comment, like, userId) {
                            likes[userId] = like
                            comment.likes = likes
                            commentAdapter.refresh()
                        }
                    }
                }
            }
        }) { state ->
            Log.i(TAG, "setupAdapter: $state")
            if (state == LOADED || state == FINISHED) {
                if (commentAdapter.itemCount != 0) {
                    binding.noCommentLayout.visibility = View.GONE
                } else {
                    binding.noCommentLayout.visibility = View.VISIBLE
                }
                viewModel.setRefreshing(false)
            } else if (state == LOADING_MORE || state == LOADING_MORE) {
                viewModel.setRefreshing(true)
            }
        }

        binding.commentRv.adapter = commentAdapter
    }

    private fun setupEditText() {
        if (!viewModel.isWordDetailSet()) return
        val word = viewModel.getWordDetail().value!!
        binding.commentEt.apply {
            hint = getString(R.string.write_quote, word.name)
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
        viewModel.getUserMessage()
            .observe(this, Observer {
                TPUtils.showSnackBar(this, it)
            })

        viewModel.isRefreshing()
            .observe(this) {
                it?.let {
                    binding.swipeRefreshLayout.isRefreshing = it
                }
            }
        viewModel.getWordDetail()
            .observe(this) {
                it?.let { word ->
                    setupUi(word)
                }
            }
        viewModel.getRefreshComment()
            .observe(this) {
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
        }
        word.likes?.let {
            val user = viewModel.getPrefUser()
            if (user?.id != null && it[user.id] != null && it[user.id] == true) {
                binding.likeBtn.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_like_black
                )
            } else {
                binding.likeBtn.background = ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_like_border_black
                )
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
        val user = UserHelper.getUser()
        if (user == null) {
            viewModel.setUserMessage(getString(R.string.error_login))
            return false
        }
        if (!viewModel.isWordDetailSet()) return false
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
                            MaterialAlertDialogBuilder(
                                this@WordDetailActivity,
                                R.style.AlertDialogTheme
                            )
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
        bottomSheet.show(supportFragmentManager, OptionsBottomFragment.TAG)
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
                    when (option) {
                        Option.Edit -> {
                            if (!viewModel.isWordDetailSet()) return
                            val word = viewModel.getWordDetail().value!!
                            val intent = WordActivity.getIntent(
                                context = this@WordDetailActivity,
                                word = word,
                                mode = WordActivity.KEY_MODE_EDIT
                            )
                            resultLauncher.launch(intent)
                        }
                    }
                }
            }
        )
        bottomSheet.show(supportFragmentManager, OptionsBottomFragment.TAG)
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
        binding.swipeRefreshLayout.setOnRefreshListener {
            setupUI()
        }
        binding.likeBtn.setOnClickListener {
            viewModel.likeWord()
        }
    }

    // endregion
}
