package com.tarripoha.android.ui.word

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.tarripoha.android.R
import com.tarripoha.android.data.db.Word
import com.tarripoha.android.databinding.ActivityWordBinding
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.util.TPUtils

class WordActivity : BaseActivity() {

    private lateinit var binding: ActivityWordBinding

    private lateinit var displayMode: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpListeners()
        checkForPreFilledData()
    }

    private fun checkForPreFilledData() {

        if (intent.hasExtra(KEY_WORD)) {
            val word = intent?.getParcelableExtra<Word>(KEY_WORD)
            binding.etName.setText(word?.name)
            binding.etName.isEnabled = false
            binding.etMeaning.setText(word?.meaning)
            displayMode = intent?.getStringExtra(KEY_MODE) ?: KEY_MODE_NEW
            title = if (displayMode == KEY_MODE_EDIT) {
                binding.btnAdd.text = getString(R.string.save)
                getString(R.string.edit)
            } else {
                getString(R.string.add_new_word)
            }
        } else {
            TPUtils.showSnackBar(this, getString(R.string.error_unknown))
        }
    }

    private fun setUpListeners() {
        binding.btnAdd.setOnClickListener {
            if (
                binding.etName.text.trim()
                    .isEmpty() ||
                binding.etMeaning.text.trim()
                    .isEmpty()
            ) {
                TPUtils.showSnackBar(this, getString(R.string.empty_field))
                return@setOnClickListener
            }
            val word = Word(
                name = binding.etName.text.toString()
                    .trim(),
                meaning = binding.etMeaning.text.toString()
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
        private const val KEY_MODE = "mode"
        const val KEY_MODE_NEW = "new"
        const val KEY_MODE_EDIT = "edit"

        fun getIntent(
            context: Context,
            word: Word,
            mode: String = KEY_MODE_NEW,
        ): Intent {
            val intent = Intent(context, WordActivity::class.java)
            intent.putExtra(KEY_MODE, mode)
            intent.putExtra(KEY_WORD, word)
            return intent
        }
    }
}
