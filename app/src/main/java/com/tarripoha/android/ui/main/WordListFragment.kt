package com.tarripoha.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.data.model.Word
import com.tarripoha.android.databinding.LayoutRvWithSwipeBinding
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.ui.word.WordDetailActivity

class WordListFragment : Fragment() {

    // region Variables

    private lateinit var factory: ViewModelProvider.Factory
    private lateinit var binding: LayoutRvWithSwipeBinding
    private lateinit var wordAdapter: WordAdapter
    private val viewModel by activityViewModels<MainViewModel> {
        factory
    }

    // endregion

    // region Fragment Related Methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LayoutRvWithSwipeBinding
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
        fetchAllWord()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.clear()
        super.onPrepareOptionsMenu(menu)
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
                    viewModel.updateViewsCount(word = data)
                    WordDetailActivity.startMe(
                        context = requireContext(),
                        wordDetail = data
                    )
                }
            })
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: RecyclerView,
                dx: Int,
                dy: Int
            ) {
                val position = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                binding.swipeRefreshLayout.isEnabled = position <= 0
            }
        }
        binding.withSwipeRv.apply {
            layoutManager = linearLayoutManager
            adapter = wordAdapter
            addOnScrollListener(scrollListener)
        }
    }

    private fun setupObservers() {
        viewModel.apply {
            isRefreshing()
                .observe(viewLifecycleOwner, Observer {
                    it?.let {
                        binding.swipeRefreshLayout.isRefreshing = it
                    }
                })
            getWords()
                .observe(viewLifecycleOwner, Observer {
                    it?.let {
                        wordAdapter.setWordList(it)
                    }
                })
        }
    }

    private fun fetchAllWord() {
        val param = viewModel.getWordListParam().value
        if (param != null) {
            viewModel.fetchWords(param)
        } else {
            viewModel.setUserMessage(getString(R.string.error_unknown))
        }
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchAllWord()
        }
    }

    // endregion

    // Helper Class

    data class WordListFragmentParam(val lang: String, val category: String)

    // endregion
}