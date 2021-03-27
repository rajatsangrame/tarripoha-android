package com.tarripoha.android.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Comment
import com.tarripoha.android.databinding.FragmentWordDetailBinding
import com.tarripoha.android.util.ItemClickListener

class WordDetailFragment : Fragment() {

  // region Variables

  companion object {
    private const val TAG = "WordDetailFragment"
  }

  private lateinit var factory: ViewModelProvider.Factory
  private lateinit var binding: FragmentWordDetailBinding
  private lateinit var commentAdapter: CommentAdapter
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
    binding.commentEt.doAfterTextChanged {
      checkPostBtnColor(it.toString())
    }
  }

  private fun checkPostBtnColor(query: String) {
    if (query.isNotEmpty()) {
      binding.postCommentBtn.apply {
        setBackgroundResource(R.drawable.ic_send_black)
        isEnabled = true
      }
    } else {
      binding.postCommentBtn.apply {
        setBackgroundResource(R.drawable.ic_send_grey)
        isEnabled = false
      }
    }
  }

  private fun setupRecyclerView() {
    val linearLayoutManager = LinearLayoutManager(
        context, RecyclerView.VERTICAL, false
    )
    commentAdapter = CommentAdapter(ArrayList(), object : ItemClickListener<Comment> {
      override fun onClick(
        position: Int,
        data: Comment
      ) {
        // no-op
      }
    })
    binding.commentRv.apply {
      layoutManager = linearLayoutManager
      adapter = commentAdapter
    }
  }

  private fun setupObservers() {
    viewModel.getWordDetail()
        .observe(viewLifecycleOwner) {
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
    binding.postCommentBtn.setOnClickListener {

    }
  }

  // endregion
}
