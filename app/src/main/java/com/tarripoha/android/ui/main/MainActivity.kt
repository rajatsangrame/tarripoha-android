package com.tarripoha.android.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.*
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tarripoha.android.TPApp
import com.tarripoha.android.R
import com.tarripoha.android.R.color
import com.tarripoha.android.databinding.LayoutToolbarWithNavigationBinding
import com.tarripoha.android.di.component.DaggerMainActivityComponent
import com.tarripoha.android.di.component.MainActivityComponent
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.ViewModelFactory
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity() {

  @Inject
  lateinit var factory: ViewModelFactory
  private lateinit var navController: NavController
  private lateinit var viewModel: MainViewModel
  private lateinit var binding: LayoutToolbarWithNavigationBinding
  private var compositeDisposable = CompositeDisposable()

  companion object {
    private const val SEARCH_DEBOUNCE_TIME_IN_MS = 300L
    private const val TAG = "MainActivity"

    fun startMe(context: Context) {
      context.startActivity(Intent(context, MainActivity::class.java))
    }
  }

  // region Activity Related

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = LayoutToolbarWithNavigationBinding.inflate(layoutInflater)
    setContentView(binding.root)
    getDependency()
    viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
    navController = findNavController(R.id.nav_host_fragment)
    navController.setGraph(R.navigation.main_nav_graph)
    handleNavigation()
    setupUI()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater: MenuInflater = menuInflater
    inflater.inflate(R.menu.menu_main, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      android.R.id.home -> {
        hideKeyboard()
        super.onBackPressed()
        true
      }
      R.id.menu_search -> {
        binding.toolbarLayout.searchEt.text = null
        navController.navigate(R.id.action_HomeFragment_to_SearchFragment)
        true
      }
      R.id.menu_info -> {
        val count = if (viewModel.getWordCount().value == null) {
          0
        } else {
          viewModel.getWordCount().value
        }
        TPUtils.showSnackBar(this, "Total words: $count")
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  override fun onDestroy() {
    compositeDisposable.dispose()
    super.onDestroy()
  }

  //endregion

  //region Helper functions

  private fun setupUI() {
    setupToolbar()
    setupObservers()
    setupListeners()
    setupSearchEditText()
  }

  private fun setupToolbar() {
    setSupportActionBar(binding.toolbarLayout.toolbar)
    supportActionBar?.apply {
      title = null
      setDisplayHomeAsUpEnabled(false)
    }
  }

  private fun getDependency() {
    val component: MainActivityComponent = DaggerMainActivityComponent
        .builder()
        .applicationComponent(
            TPApp.get(this)
                .getComponent()
        )
        .build()
    component.injectMainActivity(this)
  }

  private fun setupObservers() {
    viewModel.getUserMessage()
        .observe(this, Observer {
          TPUtils.showSnackBar(this, it)
        })
  }

  private fun handleNavigation() {
    navController.addOnDestinationChangedListener { _, destination, _ ->
      when (destination.id) {
        R.id.nav_home -> {
          supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this@MainActivity, color.colorPrimary))
            )
          }
          binding.toolbarLayout.apply {
            title.text = getString(R.string.app_name)
            title.visibility = View.VISIBLE
            searchToolbar.visibility = View.GONE
          }
        }

        R.id.nav_search -> {
          supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_grey)
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this@MainActivity, color.colorToolbarWhite))
            )
          }
          binding.toolbarLayout.apply {
            title.visibility = View.GONE
            searchToolbar.visibility = View.VISIBLE
          }
          showKeyboard()
        }
        R.id.nav_word_detail -> {
          supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this@MainActivity, color.colorPrimary))
            )
          }
          binding.toolbarLayout.apply {
            title.visibility = View.GONE
            title.visibility = View.GONE
            searchToolbar.visibility = View.GONE
          }
        }
      }
    }
  }

  private fun setupSearchEditText() {

    binding.toolbarLayout.searchEt.apply {
      val d = RxTextView.textChanges(this)
          .subscribeOn(AndroidSchedulers.mainThread())
          .debounce(SEARCH_DEBOUNCE_TIME_IN_MS, TimeUnit.MILLISECONDS)
          .observeOn(AndroidSchedulers.mainThread())
          .subscribe {
            viewModel.setQuery(it.toString())
          }
      setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          // no-op
        }
        false
      }
      compositeDisposable.add(d)
      doAfterTextChanged {
        it?.let { editable ->
          checkClearBtnVisibility(editable.toString())
        }
      }
    }
  }

  private fun checkClearBtnVisibility(query: String) {
    if (query.isNotEmpty()) binding.toolbarLayout.clearBtn.visibility = View.VISIBLE
    else binding.toolbarLayout.clearBtn.visibility = View.GONE
  }

  private fun setupListeners() {
    binding.toolbarLayout.apply {
      clearBtn.setOnClickListener {
        showKeyboard()
        searchEt.text = null
        clearBtn.visibility = View.GONE
      }
    }
  }

  private fun showKeyboard() {
    TPUtils.showKeyboard(context = this, view = binding.toolbarLayout.searchEt)
  }

  private fun hideKeyboard() {
    TPUtils.hideKeyboard(context = this, view = binding.toolbarLayout.searchEt)
  }

  //endregion

}