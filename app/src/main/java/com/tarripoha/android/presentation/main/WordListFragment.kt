package com.tarripoha.android.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.Constants
import com.tarripoha.android.R
import com.tarripoha.android.databinding.FragmentWordListBinding
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.presentation.wordinfo.WordInfoActivity
import com.tarripoha.android.util.ItemClickListener
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WordListFragment : Fragment() {

    // region Variables

    private lateinit var binding: FragmentWordListBinding
    private lateinit var wordAdapter: WordAdapter
    private val viewModel: MainViewModel by activityViewModels()

    // endregion

    // region Fragment Related Methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWordListBinding
            .inflate(LayoutInflater.from(requireContext()), container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        viewModel.fetchInitialWordList()
    }

    // endregion

    // region Helper Methods

    private fun setupUI() {
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupToolbar() {
        binding.toolbarLayout.title.text =
            viewModel.wordListParam?.heading ?: getString(R.string.words)
        binding.toolbarLayout.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        val param = viewModel.wordListParam
        val option = WordAdapter.ViewingOptions()
        if (param != null && param.category == Constants.CATEGORY_USER_REQUESTED) {
            option.showStatus = true
        }
        val linearLayoutManager = LinearLayoutManager(
            context, RecyclerView.VERTICAL, false
        )
        wordAdapter =
            WordAdapter(words = ArrayList(), itemClickListener = object : ItemClickListener<Word> {
                override fun onClick(
                    position: Int,
                    data: Word
                ) {
                    if (data.isApproved() || param?.category == Constants.CATEGORY_PENDING_APPROVALS) {
                        WordInfoActivity.startMe(
                            context = requireContext(),
                            word = data.name
                        )
                    } else viewModel.setUserMessage(getString(R.string.msg_word_not_approved))
                }
            }, options = option)
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
            showProgress.observe(viewLifecycleOwner, Observer {
                binding.shimmer.visibility = if (it == true) {
                    binding.shimmer.startShimmer()
                    View.VISIBLE
                } else {
                    binding.shimmer.stopShimmer()
                    View.GONE
                }
            })
            getWords()
                .observe(viewLifecycleOwner, Observer {
                    it?.let {
                        wordAdapter.setWordList(it)
                    }
                })
            getWordListErrorMsg()
                .observe(viewLifecycleOwner, Observer {
                    if (it.isNullOrEmpty()) {
                        binding.layoutError.layoutError.visibility = View.GONE
                    } else {
                        binding.layoutError.tvErrorMsg.text = it
                        binding.layoutError.layoutError.visibility = View.VISIBLE
                    }
                })
        }
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchInitialWordList(true)
        }
    }

    // endregion

    // Helper Class

    data class WordListFragmentParam(val heading: String, val lang: String, val category: String)

    // endregion
}