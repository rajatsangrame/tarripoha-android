package com.tarripoha.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentHomeBinding
import com.tarripoha.android.firebase.*
import com.tarripoha.android.firebase.PowerStone
import com.tarripoha.android.ui.word.WordDetailActivity
import com.tarripoha.android.util.GridItemDecorator
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils.toDp

class HomeFragment : Fragment() {

    // region Variables

    private lateinit var factory: ViewModelProvider.Factory
    private lateinit var binding: FragmentHomeBinding
    private val viewModel by activityViewModels<MainViewModel> {
        factory
    }
    private val adapterMap: MutableMap<String, DashboardHelper> = mutableMapOf()

    // endregion

    // region Fragment Related Methods

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding
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

    // endregion

    // region Helper Methods

    private fun setupUI() {
        setupDashboard()
        setupListeners()
        setupObservers()
        fetchDashBoardData()
    }

    private fun setupDashboard() {
        binding.dashboardLl.removeAllViews()

        val list = PowerStone.getDashboardInfo()
        val dashboardResponseList: MutableList<DashboardResponse> = mutableListOf()
        list.forEach {
            when (it.type) {
                GlobalVar.TYPE_WORD -> {
                    if (it.key.isNullOrEmpty() || it.category.isNullOrEmpty() || it.lang.isNullOrEmpty()) {
                        viewModel.setUserMessage(getString(R.string.error_unknown))
                        return@forEach
                    }
                    val labelledRecycleView = LabelledRecycleView(requireContext())
                    val label = if (it.category == GlobalVar.CATEGORY_MOST_VIEWED) {
                        getString(R.string.trending)
                    } else getString(R.string.top_words)

                    labelledRecycleView.setOptionalText(it.lang)
                    labelledRecycleView.setLabel(label)
                    labelledRecycleView.setOnNavigateClickListener { _ ->
                        navigateToWordListFragment(lang = it.lang, category = it.category)
                    }
                    val adapter = WordAdapter(
                        words = ArrayList(),
                        squareView = true,
                        itemClickListener = object : ItemClickListener<Word> {
                            override fun onClick(position: Int, data: Word) {
                                viewModel.updateViewsCount(word = data)
                                WordDetailActivity.startMe(
                                    context = requireContext(),
                                    wordDetail = data
                                )
                            }
                        })
                    labelledRecycleView.getRecyclerView()
                        .addItemDecoration(
                            GridItemDecorator(spanCount = 5, spacing = 64.toDp, includeEdge = true)
                        )
                    labelledRecycleView.getRecyclerView().adapter = adapter
                    adapterMap[it.key] = DashboardHelper(adapter = adapter, dashboardResponse = it)
                    dashboardResponseList.add(it)
                    binding.dashboardLl.addView(labelledRecycleView)
                }
                GlobalVar.TYPE_GOOGLE_AD -> {
                    // no -op
                }
            }
        }
    }

    private fun fetchDashBoardData() {
        val list = PowerStone.getDashboardInfo()
        viewModel.fetchAllWord(list)
    }

    private fun navigateToWordListFragment(lang: String, category: String) {
        viewModel.setWordListParam(null)
        viewModel.setWords(null)
        val param = WordListFragment.WordListFragmentParam(
            lang = lang,
            category = category
        )
        viewModel.setWordListParam(param)
        findNavController().navigate(R.id.action_HomeFragment_to_WordListFragment)
    }

    private fun setupObservers() {
        viewModel.apply {
            isRefreshing()
                .observe(viewLifecycleOwner, Observer {
                    it?.let {
                        binding.swipeRefreshLayout.isRefreshing = it
                    }
                })
            getDashboardData()
                .observe(viewLifecycleOwner, Observer {
                    adapterMap.forEach { map ->
                        it[map.key]?.let { words ->
                            val adapter = map.value.adapter
                            adapter.setWordList(words = words)
                        }
                    }
                })
        }
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchDashBoardData()
        }
    }

    // endregion

    // Helper Class

    private data class DashboardHelper(
        val adapter: WordAdapter,
        val dashboardResponse: DashboardResponse
    )

    // endregion
}
