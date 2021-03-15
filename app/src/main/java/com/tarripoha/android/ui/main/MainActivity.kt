package com.tarripoha.android.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.App
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.ActivityMainBinding
import com.tarripoha.android.di.component.DaggerMainActivityComponent
import com.tarripoha.android.di.component.MainActivityComponent
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.ui.add.WordActivity
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.ViewModelFactory
import javax.inject.Inject

class MainActivity : BaseActivity() {

  @Inject
  lateinit var factory: ViewModelFactory

  private lateinit var viewModel: MainViewModel
  private lateinit var binding: ActivityMainBinding
  private lateinit var wordAdapter: WordAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    getDependency()
    viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

    init()
    fetchAllWord()
  }

  private fun init() {
    setUpRecycleView()
    setUpListeners()
    setupObservers()
  }

  private fun fetchAllWord() {
    viewModel.fetchAllWord()
  }

  private fun setUpRecycleView() {
    val linearLayoutManager = LinearLayoutManager(
        this, RecyclerView.VERTICAL, false
    )
    binding.layout.withSwipeRv.layoutManager = linearLayoutManager
    binding.layout.withSwipeRv.isNestedScrollingEnabled = false
    wordAdapter = WordAdapter(ArrayList(), object : ItemClickListener<Word> {
      override fun onClick(
        position: Int,
        data: Word
      ) {
        val intent = Intent(this@MainActivity, WordActivity::class.java)
        intent.putExtra(WordActivity.KEY_WORD, data)
        startActivityForResult(intent, REQUEST_CODE_WORD)
      }
    })
    binding.layout.withSwipeRv.adapter = wordAdapter
    binding.layout.withSwipeRv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int
      ) {
        val position = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        binding.layout.swipeRefreshLayout.isEnabled = position <= 0
      }
    })
    val callback = Callback()
    val itemTouchHelper = ItemTouchHelper(callback)
    //itemTouchHelper.attachToRecyclerView(with_swipe_rv)
  }

  private fun setupObservers() {
    viewModel.isRefreshing()
        .observe(this, Observer {
          it.let {
            binding.layout.swipeRefreshLayout.isRefreshing = it
          }
        })
    viewModel.getAllWords()
        .observe(this, Observer {
          it?.let {
            wordAdapter.setWordList(it)
          }
        })
    viewModel.getUserMessage()
        .observe(this, Observer {
          TPUtils.showSnacBar(this, it)
        })
  }

  private fun getDependency() {
    val component: MainActivityComponent = DaggerMainActivityComponent
        .builder()
        .applicationComponent(
            App.get(this)
                .getComponent()
        )
        .build()
    component.injectMainActivity(this)
  }

  private fun setUpListeners() {
    binding.btnAdd.setOnClickListener {
      startActivityForResult(Intent(this, WordActivity::class.java), REQUEST_CODE_WORD)
    }
    binding.layout.swipeRefreshLayout.setOnRefreshListener {
      fetchAllWord()
    }
  }

  override fun onActivityResult(
    requestCode: Int,
    resultCode: Int,
    data: Intent?
  ) {
    if (requestCode == REQUEST_CODE_WORD && resultCode == RESULT_OK) {
      val word = data?.getParcelableExtra<Word>(WordActivity.KEY_WORD)
      if (word is Word) {
        viewModel.addWord(word)
      }
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  inner class Callback : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder
    ): Int {
      return makeMovementFlags(0, ItemTouchHelper.RIGHT)
    }

    override fun onMove(
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder,
      target: RecyclerView.ViewHolder
    ): Boolean {
      return false
    }

    override fun onSwiped(
      viewHolder: RecyclerView.ViewHolder,
      direction: Int
    ) {
    }
  }

  companion object {
    private const val TAG = "MainActivity"
    private const val REQUEST_CODE_WORD = 101
  }
}