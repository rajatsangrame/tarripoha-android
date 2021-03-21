package com.tarripoha.android.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tarripoha.android.App
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.FragmentSearchBinding
import com.tarripoha.android.ui.add.WordActivity
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

/**
 * Fragment to show all the cities.
 */
class SearchFragment : Fragment() {

  // region Variables

  companion object {
    private const val TAG = "SearchFragment"
    private const val REQUEST_CODE_WORD = 101
    private const val SEARCH_DEBOUNCE_TIME_IN_MS = 300L
  }

  private lateinit var factory: ViewModelProvider.Factory
  private lateinit var binding: FragmentSearchBinding
  private lateinit var wordAdapter: WordAdapter
  private var compositeDisposable = CompositeDisposable()
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

  override fun onDestroyView() {
    compositeDisposable.dispose()
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
    setupSearchEditText()
    showKeyboard()
  }

  private fun setupSearchEditText() {

    binding.searchEt.apply {
      val d = RxTextView.textChanges(this)
          .subscribeOn(AndroidSchedulers.mainThread())
          .debounce(SEARCH_DEBOUNCE_TIME_IN_MS, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
            viewModel.setQuery(it.toString())
          }
      setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          TPUtils.hideKeyboard(requireContext(), this)
        }
        true
      }
      compositeDisposable.add(d)
    }
  }

  private fun showKeyboard() {
    TPUtils.showKeyboard(binding.searchEt, requireContext())
  }

  private fun displayAddWordPlank() {
    val query = binding.searchEt.text.toString()
    if (query.isNotEmpty()) {
      wordAdapter.displayNewWordPlank(query)
    }
  }

  private fun setupRecyclerView() {
    val linearLayoutManager = LinearLayoutManager(
        context, RecyclerView.VERTICAL, false
    )
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
              if (it.isEmpty()) {
                displayAddWordPlank()
              }
            }
          })

      getQuery()
          .observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "setupObservers: getQuery: $it")
            it?.let {
              if (it.isEmpty()) {
                binding.clearBtn.visibility = View.GONE
                viewModel.setSearchWords(ArrayList())
              } else {
                binding.clearBtn.visibility = View.VISIBLE
                viewModel.search(it)
              }
            }
          })
    }
  }

  // endregion

  // region Click Related Methods

  private fun setupListeners() {
    binding.backBtn.setOnClickListener {
      findNavController().popBackStack()
    }
    binding.clearBtn.setOnClickListener {
      showKeyboard()
      binding.searchEt.setText("")
      binding.clearBtn.visibility = View.GONE
    }
  }

  // endregion
}