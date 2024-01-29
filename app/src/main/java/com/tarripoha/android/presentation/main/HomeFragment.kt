package com.tarripoha.android.presentation.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE
import com.google.android.play.core.install.model.UpdateAvailability
import com.tarripoha.android.Constants
import com.tarripoha.android.Constants.DashboardViewCategory
import com.tarripoha.android.Constants.DashboardViewType
import com.tarripoha.android.R
import com.tarripoha.android.data.model.DashboardResponse
import com.tarripoha.android.data.model.LabeledView
import com.tarripoha.android.databinding.FragmentHomeBinding
import com.tarripoha.android.domain.entity.Word
import com.tarripoha.android.presentation.main2.MainViewModel
import com.tarripoha.android.util.GridItemDecorator
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils.toDp
import com.tarripoha.android.util.helper.PreferenceHelper
import com.tarripoha.android.util.ktx.showDialog
import timber.log.Timber
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    // region Variables

    companion object {
        const val UPDATE_REQUEST_CODE = 101
    }

    private lateinit var factory: ViewModelProvider.Factory
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MainViewModel by activityViewModels()
    private val adapterMap: MutableMap<String, DashboardHelper> = mutableMapOf()
    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(requireActivity())
    }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    override fun onResume() {
        super.onResume()
        checkUpdateAvailable()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE && resultCode != Activity.RESULT_OK) {
            viewModel.setUserMessage(getString(R.string.error_update_process_failed))
        }
    }

    // endregion

    // region Helper Methods

    private fun setupUI() {
        setupToolbar()
        setupListeners()
        setupObservers()
        fetchDashBoardData(startShimmer = true)
    }

    private fun setupToolbar() {
        binding.toolbarLayout.btnBack.setImageResource(R.drawable.ic_hamburger)
        binding.toolbarLayout.btnBack.setOnClickListener {
            (requireActivity() as MainActivity).openDrawer()
        }
    }

    private fun setupDashboard(dashboardInfo: DashboardResponse) {
        binding.dashboardLl.removeAllViews()

        binding.wordOfTheWeekTv.text = dashboardInfo.wordOfTheDay
        val labeledViewList: MutableList<LabeledView> = mutableListOf()
        dashboardInfo.labeledViews.forEach {
            when (it.type) {
                DashboardViewType.TYPE_WORD.value -> {
                    val key = "${it.lang!!}_${it.category!!}"
                    val language = Constants.getLanguageName(it.lang)!!
                    val labelledRecycleView = LabelledRecycleView(requireContext())
                    val label = if (it.category == DashboardViewCategory.MOST_VIEWED.value) {
                        getString(R.string.trending)
                    } else getString(R.string.top_words)

                    labelledRecycleView.setOptionalText(language)
                    labelledRecycleView.setLabel(label)
                    labelledRecycleView.setOnNavigateClickListener { _ ->
                        navigateToWordListFragment(
                            lang = language,
                            category = it.category,
                            heading = "$label : $language"
                        )
                    }
                    val adapter = WordAdapter(
                        words = ArrayList(),
                        options = WordAdapter.ViewingOptions(squareView = true),
                        itemClickListener = object : ItemClickListener<Word> {
                            override fun onClick(position: Int, data: Word) {
//                                WordDetailActivity.startMe(
//                                    context = requireContext(),
//                                    wordDetail = data
//                                )
                            }
                        })
                    labelledRecycleView.getRecyclerView()
                        .addItemDecoration(
                            GridItemDecorator(spanCount = 5, spacing = 64.toDp, includeEdge = true)
                        )
                    labelledRecycleView.getRecyclerView().adapter = adapter
                    adapterMap[key] = DashboardHelper(
                        adapter = adapter,
                        labelledRecycleView = labelledRecycleView
                    )
                    labeledViewList.add(it)
                    binding.dashboardLl.addView(labelledRecycleView)
                }

                DashboardViewType.TYPE_GOOGLE_AD.value -> {
                    // no -op
                }
            }
        }
    }

    private fun fetchDashBoardData(startShimmer: Boolean = false) {
        if (startShimmer) binding.shimmer.startShimmer()
        viewModel.fetchDashboardWord()
    }

    private fun navigateToWordListFragment(lang: String, category: String, heading: String) {
        /*viewModel.resetWordListParams()
        val param = WordListFragment.WordListFragmentParam(
            lang = lang,
            category = category
        )
        viewModel.setWordListParam(param)
        viewModel.setToolbarHeading(heading)
        findNavController().navigate(R.id.action_HomeFragment_to_WordListFragment)*/
    }

    private fun setupObservers() {
        viewModel.apply {

            isRefreshing()
                .observe(viewLifecycleOwner, Observer {
                    it?.let {
                        if (binding.shimmer.isShimmerStarted && binding.shimmer.isShimmerVisible) return@let
                        binding.swipeRefreshLayout.isRefreshing = it
                    }
                })

            getDashBoardInfo()
                .observe(viewLifecycleOwner, Observer {
                    if (it == null) return@Observer

                    Timber.tag("FUCK").d("here " + it.data.size)

                    setupDashboard(it.response)
                    adapterMap.forEach { map ->
                        it.data[map.key]?.let { words ->
                            val adapter = map.value.adapter
                            adapter.setWordList(words = words)
                            map.value.labelledRecycleView.setErrorView(
                                if (words.isEmpty()) View.VISIBLE else View.GONE
                            )
                        }
                    }
                    binding.shimmer.apply {
                        stopShimmer()
                        visibility = View.GONE
                    }
                    binding.container.visibility = View.VISIBLE
                })
        }
    }

    private fun checkUpdateAvailable() {
        Handler(Looper.getMainLooper()).postDelayed({
            val updateAvailable = PreferenceHelper.get<Boolean>(
                PreferenceHelper.KEY_NEW_VERSION_AVAILABLE,
                false
            )
            val lastUpdateCheck = PreferenceHelper.get<Long>(
                PreferenceHelper.KEY_LAST_UPDATE_CHECK,
                0L
            )
            if (updateAvailable) {
                val current = System.currentTimeMillis()
                if (current - lastUpdateCheck > TimeUnit.HOURS.toMillis(18)) {
                    showUpdateAvailableDialog(getString(R.string.msg_update_available))
                }
            }
        }, 2000)
    }

    private fun showUpdateAvailableDialog(message: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .showDialog(
                title = getString(R.string.update_available),
                message = message,
                positiveText = getString(R.string.update),
                negativeText = getString(R.string.close),
                cancelable = false,
                positiveListener = {
                    appUpdateManager
                        .appUpdateInfo
                        .addOnSuccessListener { appUpdateInfo ->
                            if (appUpdateInfo.updateAvailability()
                                == UpdateAvailability.UPDATE_AVAILABLE
                            ) {
                                appUpdateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    IMMEDIATE,
                                    requireActivity(),
                                    UPDATE_REQUEST_CODE
                                )
                            }
                        }
                },
                negativeListener = {
                    PreferenceHelper.put<Long>(
                        PreferenceHelper.KEY_LAST_UPDATE_CHECK,
                        System.currentTimeMillis()
                    )
                }
            )
    }

    // endregion

    // region Click Related Methods

    private fun setupListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchDashBoardData()
        }
        binding.wordOfTheWeekLayout.setOnClickListener {
            val word: String = binding.wordOfTheWeekTv.text.trim().toString()
            if (word.isEmpty()) {
                viewModel.setUserMessage(getString(R.string.error_unknown))
                return@setOnClickListener
            }
            /*WordDetailActivity.startMe(
                context = requireContext(),
                postFetch = true,
                word = word
            )*/
        }
    }

    // endregion

    // Helper Class

    private data class DashboardHelper(
        val adapter: WordAdapter,
        val labelledRecycleView: LabelledRecycleView
    )

    // endregion
}
