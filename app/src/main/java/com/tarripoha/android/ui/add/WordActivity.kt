package com.tarripoha.android.ui.add

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.util.Utils
import kotlinx.android.synthetic.main.activity_word.*

class WordActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_word)

    setUpListeners()
    checkForPreFilledData()
  }

  private fun checkForPreFilledData() {

    if (intent.hasExtra(KEY_WORD)) {
      val word = intent?.getParcelableExtra<Word>(KEY_WORD)
      et_name.setText(word?.name)
      et_name.isEnabled = false
      et_meaning.setText(word?.meaning)
    }
  }

  private fun setUpListeners() {
    btn_add.setOnClickListener {
      if (
          et_name.text.trim()
              .isEmpty() ||
          et_meaning.text.trim()
              .isEmpty()
      ) {
        Utils.showToast(this, getString(R.string.empty_field))
        return@setOnClickListener
      }
      val word = Word(
          et_name.text.toString()
              .trim(),
          et_meaning.text.toString()
              .trim()
      )
      val intent = Intent()
      intent.putExtra(KEY_WORD, word)
      setResult(RESULT_OK, intent)
      finish()
    }
  }

  companion object {
    private const val TAG = "WordActivity"
    const val KEY_WORD = "word"
  }
}