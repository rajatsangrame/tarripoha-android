package com.tarripoha.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.databinding.FragmentWordDetailBinding

class WordDetailFragment : Fragment() {

  // region Variables

  companion object {
    private const val TAG = "WordDetailFragment"
  }

  private lateinit var factory: ViewModelProvider.Factory
  private lateinit var binding: FragmentWordDetailBinding
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
      ViewModelProvider.AndroidViewModelFactory(App.get(requireContext()))
    if (viewModel.getWordDetail().value == null) {
      viewModel.setUserMessage(getString(R.string.error_unknown))
      return
    }

    setupUI()
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
  }

  private fun setupObservers() {
    viewModel.getWordDetail().observe(viewLifecycleOwner) {
      it.let { word ->
        binding.wordTv.text = word.name
        binding.meaningTv.text = word.meaning
      }
    }
  }

  // endregion

  // region Click Related Methods

  private fun setupListeners() {
    binding.backBtn.setOnClickListener {
      findNavController().popBackStack()
    }
    binding.searchBtn.setOnClickListener {
      findNavController().navigate(R.id.action_WordDetailFragment_to_SearchFragment)
    }
  }

  // endregion
}
