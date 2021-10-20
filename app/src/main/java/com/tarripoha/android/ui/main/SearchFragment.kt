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
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentSearchBinding
import com.tarripoha.android.ui.word.WordActivity
import com.tarripoha.android.ui.word.WordDetailActivity
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils

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
                    if (!word.name.isNullOrEmpty() && user != null && user.id != null) {
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
        viewModel.apply {
            setQuery(null)
            setSearchWords(null)
        }
        super.onDestroy()
    }

    // endregion

    // region Helper Methods

    private fun setupUI() {
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        wordAdapter =
            WordAdapter(words = ArrayList(), itemClickListener = object : ItemClickListener<Word> {
                override fun onClick(
                    position: Int,
                    data: Word
                ) {
                    if (data.type == Word.TYPE_NEW_WORD) {
                        if (viewModel.isUserLogin()) {
                            val intent = WordActivity.getIntent(
                                context = requireContext(),
                                word = data,
                                mode = WordActivity.KEY_MODE_NEW
                            )
                            resultLauncher.launch(intent)
                        } else {
                            viewModel.setUserMessage(getString(R.string.error_login))
                        }
                    } else {
                        TPUtils.hideKeyboard(requireContext(), binding.root)
                        WordDetailActivity.startMe(
                            context = requireContext(),
                            wordDetail = data
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
                        if (it.isEmpty()) {
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
}
