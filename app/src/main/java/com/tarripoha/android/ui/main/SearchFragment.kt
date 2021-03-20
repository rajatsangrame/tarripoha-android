package com.tarripoha.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.App
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentHomeBinding
import com.tarripoha.android.databinding.FragmentSearchBinding
import com.tarripoha.android.ui.add.WordActivity
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils

/**
 * Fragment to show all the cities.
 */
class SearchFragment : Fragment() {

  // region Variables

  companion object {
    private const val REQUEST_CODE_WORD = 101
  }

  private lateinit var factory: ViewModelProvider.Factory
  private lateinit var binding: FragmentSearchBinding
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
      ViewModelProvider.AndroidViewModelFactory(App.get(requireContext()))

    setupUI()
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == REQUEST_CODE_WORD && resultCode == AppCompatActivity.RESULT_OK) {
      val word = data?.getParcelableExtra<Word>(WordActivity.KEY_WORD)
      if (word is Word) {
        viewModel.addWord(word)
      }
    }
    super.onActivityResult(requestCode, resultCode, data)

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
    binding.wordsRv.layoutManager = linearLayoutManager
    wordAdapter = WordAdapter(ArrayList(), object : ItemClickListener<Word> {
      override fun onClick(
        position: Int,
        data: Word
      ) {
        val intent = Intent(context, WordActivity::class.java)
        intent.putExtra(WordActivity.KEY_WORD, data)
        startActivityForResult(intent, REQUEST_CODE_WORD)
      }
    })
    binding.wordsRv.adapter = wordAdapter
  }

  private fun setupObservers() {

  }

  private fun searchWord() {

  }

  // endregion

  // region Click Related Methods

  private fun setupListeners() {
    binding.backBtn.setOnClickListener{
      findNavController().popBackStack()
    }
  }

  // endregion
}
