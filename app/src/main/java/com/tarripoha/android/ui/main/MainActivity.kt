package com.tarripoha.android.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.di.component.DaggerMainActivityComponent
import com.tarripoha.android.di.component.MainActivityComponent
import com.tarripoha.android.di.module.MainActivityModule
import com.tarripoha.android.ui.add.WordActivity
import com.tarripoha.android.util.ItemClickListener
import com.tarripoha.android.util.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_rv_with_swipe.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

  @Inject
  lateinit var factory: ViewModelFactory

  private lateinit var viewModel: MainViewModel

  private lateinit var wordAdapter: WordAdapter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
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
    with_swipe_rv.layoutManager = linearLayoutManager
    with_swipe_rv.isNestedScrollingEnabled = false
    wordAdapter = WordAdapter(ArrayList(),object: ItemClickListener<Word>{
      override fun onClick(
        position: Int,
        data: Word
      ) {

      }
    })
    with_swipe_rv.adapter = wordAdapter
    with_swipe_rv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int
      ) {
        val position = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        swipe_refresh_layout.isEnabled = position <= 0
      }
    })
  }

  private fun setupObservers() {
    viewModel.isRefreshing()
        .observe(this, Observer {
          it.let {
            swipe_refresh_layout.isRefreshing = it
          }
        })
    viewModel.getAllWords()
        .observe(this, Observer {
          it?.let {
            wordAdapter.setWordList(it)
          }
        })
  }

  private fun getDependency() {
    val component: MainActivityComponent = DaggerMainActivityComponent
        .builder()
        .applicationComponent(
            App.get(this)
                .getComponent()
        )
        .mainActivityModule(MainActivityModule(this))
        .build()
    component.injectMainActivity(this)
  }

  private fun setUpListeners() {
    btn_add.setOnClickListener {
      startActivityForResult(Intent(this, WordActivity::class.java), REQUEST_CODE_WORD)
    }
    swipe_refresh_layout.setOnRefreshListener {
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

  companion object {
    private const val TAG = "MainActivity"
    private const val REQUEST_CODE_WORD = 101
  }
}