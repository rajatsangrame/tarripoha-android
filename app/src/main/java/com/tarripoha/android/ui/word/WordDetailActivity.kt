package com.tarripoha.android.ui.word

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.browser.customtabs.CustomTabsIntent
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
import com.tarripoha.android.data.model.Comment
import com.tarripoha.android.data.model.Word
import com.tarripoha.android.databinding.FragmentWordDetailBinding
import com.tarripoha.android.di.component.DaggerWordDetailActivityComponent
import com.tarripoha.android.di.component.WordDetailActivityComponent
import com.tarripoha.android.paging.CommentPagingAdapter
import com.tarripoha.android.paging.CommentPagingAdapter.ClickMode
import com.tarripoha.android.paging.CommentPagingAdapter.OnCommentClickListener
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.ui.OptionsBottomFragment
import com.tarripoha.android.ui.OptionsBottomFragment.Option
import com.tarripoha.android.ui.OptionsBottomFragment.OptionCLickListener
import com.tarripoha.android.ui.share.WordCardActivity
import com.tarripoha.android.util.helper.UserHelper
import com.tarripoha.android.ui.word.WordViewModel.FetchMode
import com.tarripoha.android.util.*
import com.tarripoha.android.util.texttospeech.TextToSpeechUtil
import javax.inject.Inject

class WordDetailActivity : BaseActivity() {

    // region Variables

    companion object {
        private const val TAG = "WordDetailFragment"
        private const val KEY_WORD_DETAIL = "word_detail"
        private const val KEY_WORD = "word"
        private const val KEY_POST_FETCH = "post_fetch"

        fun startMe(
            context: Context,
            wordDetail: Word? = null,
            postFetch: Boolean = false,
            word: String? = null
        ) {
            val intent = Intent(context, WordDetailActivity::class.java)
            intent.putExtra(KEY_WORD_DETAIL, wordDetail)
            intent.putExtra(KEY_POST_FETCH, postFetch)
            intent.putExtra(KEY_WORD, word)
            context.startActivity(intent)
        }
    }

    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var binding: FragmentWordDetailBinding
    private var commentAdapter: CommentPagingAdapter? = null
    private lateinit var viewModel: WordViewModel
    private var alreadyInitialised = false

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

        init()
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
        commentAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        commentAdapter?.stopListening()
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

    /**
     * This can be called again while swipe refresh. So only loading data for this condition
     */
    private fun init() {
        if (intent.hasExtra(KEY_POST_FETCH)) {
            val postShare = intent?.getBooleanExtra(KEY_POST_FETCH, false)!!
            val word = intent?.getStringExtra(KEY_WORD)
            when {
                postShare -> {
                    if (word == null) {
                        viewModel.setUserMessage(getString(R.string.error_unknown))
                        return
                    }
                    viewModel.fetchWordDetail(word = word)
                    if (!alreadyInitialised) setupUi() else commentAdapter?.refresh()
                }
                intent.hasExtra(KEY_WORD_DETAIL) -> {
                    val wordDetail = intent?.getParcelableExtra<Word>(KEY_WORD_DETAIL)
                    viewModel.setWordDetail(wordDetail)
                    if (!alreadyInitialised) setupUi() else commentAdapter?.refresh()
                }
                else -> {
                    viewModel.setUserMessage(getString(R.string.error_unknown))
                }
            }
        } else {
            viewModel.setUserMessage(getString(R.string.error_unknown))
        }
        alreadyInitialised = true
    }

    private fun setupUi() {
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
            this, RecyclerView.VERTICAL, true
        )
        binding.commentRv.apply {
            layoutManager = linearLayoutManager
        }
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
        if (commentAdapter != null) {
            return
        }
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
                            commentAdapter?.refresh()
                        }
                    }
                }
            }
        }) { state ->
            Log.i(TAG, "setupAdapter: $state")
            if (state == LOADED || state == FINISHED) {
                if (commentAdapter?.itemCount != 0) {
                    binding.noCommentLayout.layoutError.visibility = View.GONE
                } else {
                    binding.noCommentLayout.tvErrorMsg.text = getString(R.string.error_no_quotes)
                    binding.noCommentLayout.layoutError.visibility = View.VISIBLE
                }
                viewModel.setRefreshing(false)
            } else if (state == LOADING_MORE || state == LOADING_MORE) {
                viewModel.setRefreshing(true)
            }
        }

        binding.commentRv.adapter = commentAdapter
    }

    private fun setupEditText() {
        binding.commentEt.apply {
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
                    commentAdapter?.refresh()
                }
            }
    }

    private fun setupUi(word: Word) {
        setupTextToSpeech(word)
        binding.apply {
            wordTv.text = word.name
            meaningTv.text = word.meaning
            engMeaningTv.underlinedWithVisibility(word.eng)
            commentEt.hint = getString(R.string.write_quote, word.name)
        }
        binding.langTv.setTextWithVisibility(word.lang)
        TPUtils.showTotalLikes(likes = word.likes, view = binding.likeTv)
        word.setupLikes()
        word.setupSavedState()
        setupAdapter(getOption(word))
    }

    private fun Word.setupLikes() {
        this.likes?.let {
            val user = viewModel.getPrefUser()
            if (user?.id != null && it[user.id] != null && it[user.id] == true) {
                binding.likeBtn.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@WordDetailActivity,
                        R.drawable.ic_like_red
                    )
                )
            } else {
                binding.likeBtn.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@WordDetailActivity,
                        R.drawable.ic_like_border_black
                    )
                )
            }
        }
    }

    private fun Word.setupSavedState() {
        this.saved?.let {
            val user = viewModel.getPrefUser()
            if (user?.id != null && it[user.id] != null && it[user.id] == true) {
                binding.saveBtn.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@WordDetailActivity,
                        R.drawable.ic_save_black
                    )
                )
            } else {
                binding.saveBtn.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@WordDetailActivity,
                        R.drawable.ic_save_border_black
                    )
                )
            }
        }
    }

    private fun setupTextToSpeech(word: Word) {
        val lang: String? = TPUtils.getLangCode(context = this, language = word.lang)
        if (lang.isNullOrEmpty()) {
            viewModel.setUserMessage(getString(R.string.error_language_not_supported))
            return
        }
        TextToSpeechUtil.init(context = this, lang = lang)
    }

    private fun sayMyName(word: Word) {
        val lang: String? = TPUtils.getLangCode(context = this, language = word.lang)
        if (lang.isNullOrEmpty()) {
            viewModel.setUserMessage(getString(R.string.error_language_not_supported))
            return
        }
        TextToSpeechUtil.speak(context = this, lang = lang, text = word.name)
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
                    handleCommentOptionClick(comment, option)
                }
            }
        )
        bottomSheet.show(supportFragmentManager, OptionsBottomFragment.TAG)
    }

    private fun handleCommentOptionClick(comment: Comment, option: Option) {
        Log.d(TAG, "onClick: $option} ${comment.comment}")
        when (option) {
            Option.WordCard -> {
                val comments = ArrayList<Comment>()
                comments.add(comment)
                navigateToWordCardActivity(comments = comments)
            }
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
                        },
                        negativeListener = {}
                    )
            }
            Option.Copy -> {
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("comment", comment.comment)
                clipboard.setPrimaryClip(clip)
                viewModel.setUserMessage(getString(R.string.copied_to_clipboard))
            }
            else -> {
                Log.e(TAG, "handleCommentOptionClick: Unknown case")
            }
        }
    }

    private fun getCommentOptions(comment: Comment): List<Option> {
        val options = mutableListOf<Option>()
        options.add(Option.Copy)
        options.add(Option.WordCard)
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
                    handleWordOptionClick(option)
                }
            }
        )
        bottomSheet.show(supportFragmentManager, OptionsBottomFragment.TAG)
    }

    private fun handleWordOptionClick(option: Option) {
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
            Option.WordCard -> {
                navigateToWordCardActivity()
            }
            Option.Copy -> {
                if (!viewModel.isWordDetailSet()) return
                val word = viewModel.getWordDetail().value!!
                val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("word", word.name)
                clipboard.setPrimaryClip(clip)
                viewModel.setUserMessage(getString(R.string.copied_to_clipboard))
            }
            else -> {
                Log.e(TAG, "handleWordOptionClick: Unknown case")
            }
        }
    }

    private fun getWordOptions(): List<Option> {
        val options = mutableListOf<Option>()
        options.add(Option.Copy)
        options.add(Option.WordCard)
        options.add(Option.Report)
        val isAdmin = viewModel.isUserAdmin()
        if (isAdmin) {
            options.add(Option.Edit)
            options.add(Option.Delete)
        }
        return options
    }

    private fun navigateToWordCardActivity(comments: ArrayList<Comment>? = null) {
        if (!viewModel.isWordDetailSet()) return
        val word = viewModel.getWordDetail().value!!
        WordCardActivity.startMe(context = this, word = word, comments = comments)
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        binding.postCommentBtn.setOnClickListener {
            postComment()
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            init()
        }
        binding.shareBtn.setOnClickListener {
            navigateToWordCardActivity()
        }
        binding.likeBtn.setOnClickListener {
            viewModel.likeWord()
        }
        binding.saveBtn.setOnClickListener {
            viewModel.saveWord()
        }
        binding.speechIv.setOnClickListener {
            if (!viewModel.isWordDetailSet()) return@setOnClickListener
            val word = viewModel.getWordDetail().value!!
            sayMyName(word)
        }
        binding.engMeaningTv.setOnClickListener {
            if (!viewModel.isWordDetailSet()) return@setOnClickListener
            val word = viewModel.getWordDetail().value!!
            val uri = Uri.parse("https://www.google.com/search?q=${word.eng} meaning")
            val builder = CustomTabsIntent.Builder()
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(this, uri)
        }
    }

    // endregion
}
