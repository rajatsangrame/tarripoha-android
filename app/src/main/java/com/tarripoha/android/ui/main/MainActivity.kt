package com.tarripoha.android.ui.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.di.component.DaggerMainActivityComponent
import com.tarripoha.android.di.component.MainActivityComponent
import com.tarripoha.android.di.module.MainActivityModule
import com.tarripoha.android.ui.add.WordActivity
import com.tarripoha.android.util.ViewModelFactory
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

  @Inject
  lateinit var factory: ViewModelFactory

  private lateinit var viewModel: MainViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    getDependency()
    viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)

    init()
  }

  private fun init() {
    setUpListeners()

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