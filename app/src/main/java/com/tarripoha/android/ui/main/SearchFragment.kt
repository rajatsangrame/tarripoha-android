package com.tarripoha.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentSearchBinding
import com.tarripoha.android.ui.add.WordActivity
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils

class SearchFragment : Fragment() {

  // region Variables

  companion object {
    private const val TAG = "SearchFragment"
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

  override fun onDestroyView() {
    viewModel.apply {
      setQuery(null)
      setSearchWords(null)
    }
    super.onDestroyView()
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
          TPUtils.hideKeyboard(requireContext(), binding.root)
          if (data.type == Word.TYPE_NEW_WORD) {
            val intent = Intent(context, WordActivity::class.java)
            intent.putExtra(WordActivity.KEY_WORD, data)
            startActivityForResult(intent, REQUEST_CODE_WORD)
          } else {
            viewModel.setWordDetail(word = data)
            findNavController().navigate(R.id.action_SearchFragment_to_WordDetailFragment)
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
            Log.d(TAG, "setupObservers: getSearchWords: $it")
            it?.let {
              wordAdapter.setWordList(it)
            }
          })

      getQuery()
          .observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "setupObservers: getQuery: $it")
            it?.let {
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
