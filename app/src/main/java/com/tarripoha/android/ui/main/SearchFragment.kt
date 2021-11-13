package com.tarripoha.android.ui.main

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentSearchBinding
import com.tarripoha.android.databinding.LayoutItemCharBinding
import com.tarripoha.android.ui.word.WordActivity
import com.tarripoha.android.ui.word.WordDetailActivity
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.hasEnglishChars

class SearchFragment : Fragment() {

    // region Variables

    companion object {
        private const val TAG = "SearchFragment"
    }

    private lateinit var factory: ViewModelProvider.Factory
    private lateinit var binding: FragmentSearchBinding
    private lateinit var wordAdapter: WordAdapter
    private val viewModel by activityViewModels<MainViewModel> {
        factory
    }

    private val resultLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val word = result.data?.getParcelableExtra<Word>(WordActivity.KEY_WORD)
                if (word is Word) {
                    val user = viewModel.getPrefUser()
                    if (!word.name.isNullOrEmpty() && user?.id != null) {
                        word.updateUserRelatedData(user)
                        word.timestamp = System.currentTimeMillis()
                        viewModel.addNewWord(word)
                    } else {
                        viewModel.setUserMessage(getString(R.string.error_unknown))
                    }
                }
            }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding
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

        setupUI()
    }

    override fun onDestroy() {
        viewModel.resetSearchParams()
        super.onDestroy()
    }

    // endregion

    // region Helper Methods

    private fun setupUI() {
        setCharsList()
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setCharsList() {
        val aa = object : RecyclerView.Adapter<ListItemViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListItemViewHolder {
                val binding =
                    LayoutItemCharBinding.inflate(LayoutInflater.from(context), parent, false)
                return ListItemViewHolder(binding)
            }

            override fun onBindViewHolder(holder: ListItemViewHolder, position: Int) {
                holder.binding.charTv.text = GlobalVar.getCharList()[position]
            }

            override fun getItemCount(): Int = GlobalVar.getCharList().size

        }
        binding.charsLv.adapter = aa
    }

    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        wordAdapter =
            WordAdapter(words = ArrayList(), itemClickListener = object : ItemClickListener<Word> {
                override fun onClick(
                    position: Int,
                    word: Word
                ) {
                    if (word.type == Word.TYPE_NEW_WORD) {
                        if (viewModel.isUserLogin()) {
                            if (word.name.hasEnglishChars()) {
                                viewModel.setUserMessage(
                                    getString(
                                        R.string.msg_found_english_letters,
                                        word.name
                                    )
                                )
                                return
                            } else if (word.name.length < 2) {
                                viewModel.setUserMessage(
                                    getString(
                                        R.string.msg_field_too_short
                                    )
                                )
                                return
                            }
                            val intent = WordActivity.getIntent(
                                context = requireContext(),
                                word = word,
                                mode = WordActivity.KEY_MODE_NEW
                            )
                            resultLauncher.launch(intent)
                        } else {
                            viewModel.setUserMessage(getString(R.string.error_login))
                        }
                    } else {
                        viewModel.updateViewsCount(word = word)
                        TPUtils.hideKeyboard(requireContext(), binding.root)
                        WordDetailActivity.startMe(
                            context = requireContext(),
                            wordDetail = word
                        )
                    }
                }
            })
        binding.wordsRv.apply {
            layoutManager = linearLayoutManager
            adapter = wordAdapter
        }
    }

    private fun setupObservers() {
        viewModel.apply {
            getSearchWords()
                .observe(viewLifecycleOwner, Observer {
                    it?.let {
                        Log.d(TAG, "setupObservers: getSearchWords: $it")
                        wordAdapter.setWordList(it)
                    }
                })

            getQuery()
                .observe(viewLifecycleOwner, Observer {
                    it?.let {
                        Log.d(TAG, "setupObservers: getQuery: $it")
                        if (it.isEmpty() || it.contains("null")) {
                            viewModel.setSearchWords(ArrayList())
                        } else {
                            viewModel.search(it)
                        }
                    }
                })
        }
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        // no-op
    }

    // endregion

    // Helper Class

    private inner class ListItemViewHolder(val binding: LayoutItemCharBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val c = GlobalVar.getCharList()[adapterPosition]
                viewModel.setChars(c)
            }
        }
    }

    // enregion
}
