package com.tarripoha.android.ui.main

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jakewharton.rxbinding2.widget.RxTextView
import com.tarripoha.android.R
import com.tarripoha.android.TPApp
import com.tarripoha.android.databinding.ActivityMainBinding
import com.tarripoha.android.di.component.DaggerMainActivityComponent
import com.tarripoha.android.di.component.MainActivityComponent
import com.tarripoha.android.ui.BaseActivity
import com.tarripoha.android.ui.login.LoginActivity
import com.tarripoha.android.ui.login.LoginHelper
import com.tarripoha.android.ui.main.drawer.SideNavItem
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.ViewModelFactory
import com.tarripoha.android.util.helper.PreferenceHelper
import com.tarripoha.android.util.showDialog
import com.tarripoha.android.util.toggleVisibility
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var factory: ViewModelFactory
    private lateinit var navController: NavController
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private var compositeDisposable = CompositeDisposable()
    private var showBackBtn = false

    companion object {
        private const val SEARCH_DEBOUNCE_TIME_IN_MS = 300L
        private const val TAG = "MainActivity"

        fun startMe(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
            context.startActivity(intent)
        }
    }

    // region Activity Related

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        getDependency()
        viewModel = ViewModelProvider(this, factory).get(MainViewModel::class.java)
        setupUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        menu.apply {
            findItem(R.id.menu_search).isVisible = true
            findItem(R.id.menu_info).isVisible = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                if (!showBackBtn) {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                } else {
                    hideKeyboard(binding.container.toolbarLayout.searchEt)
                    super.onBackPressed()
                }
                true
            }
            R.id.menu_search -> {
                binding.container.toolbarLayout.searchEt.text = null
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

    fun onDrawerClick(item: SideNavItem) {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        Handler(Looper.getMainLooper()).postDelayed({
            when (item.itemName) {
                getString(R.string.login_register) -> {
                    LoginActivity.startMe(this)
                }
                getString(R.string.user) -> {
                    // no-op
                }
                getString(R.string.saved) -> {
                    // no-op
                }
                getString(R.string.settings) -> {
                    // no-op
                }
                getString(R.string.rate_us) -> {
                    // no-op
                }
                getString(R.string.share) -> {
                    // no-op
                }
                getString(R.string.support) -> {
                    // no-op
                }
                getString(R.string.logout) -> {
                    MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                        .showDialog(
                            message = getString(R.string.msg_confirm_logout),
                            positiveText = getString(R.string.logout),
                            positiveListener = {
                                LoginHelper.logoutUser()
                                PreferenceHelper.clear()
                                LoginActivity.startMe(this)
                                finish()
                            }
                        )
                }
            }
        }, 300)
    }

    //endregion

    //region Helper functions

    private fun setupUI() {
        setupToolbar()
        setupObservers()
        setupListeners()
        setupSearchEditText()
        navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.main_nav_graph)
        handleNavigation()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.container.toolbarLayout.toolbar)
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
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
        component.injectActivity(this)
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
                    homeNavigation()
                }

                R.id.nav_search -> {
                    searchNavigation()
                }
            }
        }
    }

    private fun homeNavigation() {
        showBackBtn = false
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_menu_white)
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
            )
        }
        binding.container.toolbarLayout.apply {
            title.text = getString(R.string.app_name)
            title.visibility = View.VISIBLE
            searchToolbar.visibility = View.GONE
        }
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
    }

    private fun searchNavigation() {
        showBackBtn = true
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_grey)
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.colorToolbarWhite))
            )
        }
        binding.container.toolbarLayout.apply {
            title.visibility = View.GONE
            searchToolbar.visibility = View.VISIBLE
        }
        showKeyboard()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setupSearchEditText() {

        binding.container.toolbarLayout.searchEt.apply {
            val d = RxTextView.textChanges(this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(SEARCH_DEBOUNCE_TIME_IN_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    viewModel.setQuery(it.toString())
                }
            compositeDisposable.add(d)
            doAfterTextChanged {
                it?.let { editable ->
                    binding.container.toolbarLayout.clearBtn.toggleVisibility(editable)
                }
            }
        }
    }

    private fun setupListeners() {
        binding.container.toolbarLayout.apply {
            clearBtn.setOnClickListener {
                showKeyboard()
                searchEt.text = null
                clearBtn.visibility = View.GONE
            }
        }
    }

    private fun showKeyboard() {
        TPUtils.showKeyboard(context = this, view = binding.container.toolbarLayout.searchEt)
    }

    //endregion

}
