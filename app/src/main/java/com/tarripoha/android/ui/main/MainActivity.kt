package com.tarripoha.android.ui.main

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.tarripoha.android.App
import com.tarripoha.android.R
import com.tarripoha.android.databinding.ActivityMainBinding
import com.tarripoha.android.di.component.DaggerMainActivityComponent
import com.tarripoha.android.di.component.MainActivityComponent
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.ViewModelFactory
import javax.inject.Inject

class MainActivity : BaseActivity() {

  @Inject
  lateinit var factory: ViewModelFactory
  private lateinit var navController: NavController
  private lateinit var viewModel: MainViewModel
  private lateinit var binding: ActivityMainBinding

  companion object {
    private const val TAG = "MainActivity"
  }

  // region Activity Related

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    getDependency()
    viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
    navController = findNavController(R.id.nav_host_fragment)
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
      R.id.search -> {
        navController.navigate(R.id.action_HomeFragment_to_SearchFragment)
        true
      }
      else -> super.onOptionsItemSelected(item)
    }
  }

  //endregion

  // Helper functions

  private fun setupUI() {
    setupToolbar()
    setupObservers()
  }

  private fun setupToolbar() {
    setSupportActionBar(binding.toolbarLayout.toolbar)
    supportActionBar?.title = null
    supportActionBar?.setDisplayHomeAsUpEnabled(false)
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

          binding.toolbarLayout.toolbar.visibility = View.VISIBLE
          binding.toolbarLayout.title.text = getString(R.string.app_name)
        }

        R.id.nav_search -> {
          binding.toolbarLayout.toolbar.visibility = View.GONE
        }
      }
    }
  }

//endregion

}