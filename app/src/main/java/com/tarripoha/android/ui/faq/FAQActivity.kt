package com.tarripoha.android.ui.faq

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.R
import com.tarripoha.android.data.model.FAQ
import com.tarripoha.android.databinding.ActivityFaqBinding
import com.tarripoha.android.firebase.PowerStone

class FAQActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFaqBinding

    companion object {
        fun startMe(context: Context) {
            context.startActivity(Intent(context, FAQActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupRecycleView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                super.onBackPressed()
            }
        }
        return true
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        binding.toolbarLayout.title.text = getString(R.string.help)
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
        }
    }

    private fun setupRecycleView() {
        val faqAdapter = FAQAdapter()
        faqAdapter.setFAQList(getFaqList())
        val linearLayoutManager = LinearLayoutManager(
            this, RecyclerView.VERTICAL, false
        )
        binding.rvFaq.apply {
            layoutManager = linearLayoutManager
            adapter = faqAdapter
        }
    }

    private fun getFaqList(): MutableList<FAQ> {
        val response = PowerStone.getFAQResponse()
        val lang = getSelectedLanguage()
        val faqList: MutableList<FAQ> = mutableListOf()
        response.forEach {
            it.faq.forEach { faq ->
                if (faq.lang == lang) {
                    faqList.add(faq)
                }
            }
        }
        return faqList
    }

    private fun getSelectedLanguage() = "en"

}