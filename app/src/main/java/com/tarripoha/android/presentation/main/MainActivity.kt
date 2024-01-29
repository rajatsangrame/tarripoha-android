package com.tarripoha.android.presentation.main

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
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.doAfterTextChanged
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tarripoha.android.GlobalVar
import com.tarripoha.android.R
import com.tarripoha.android.databinding.ActivityMainBinding
import com.tarripoha.android.presentation.base.BaseActivity
import com.tarripoha.android.presentation.main.drawer.SideNavItem
import com.tarripoha.android.presentation.main2.MainViewModel
//import com.tarripoha.android.presentation.faq.FAQActivity
//import com.tarripoha.android.presentation.login.LoginActivity
//import com.tarripoha.android.presentation.login.LoginHelper
//import com.tarripoha.android.presentation.main.drawer.SideNavItem
import com.tarripoha.android.util.*
import dagger.hilt.android.AndroidEntryPoint
//import com.tarripoha.android.util.helper.PreferenceHelper
//import com.tarripoha.android.util.helper.UserHelper
//import com.tarripoha.android.util.texttospeech.TextToSpeechUtil
import java.util.concurrent.TimeUnit


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    private val mainViewModel : MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding
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
        setupUI()
    }

    /*override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        menu.apply {
            findItem(R.id.menu_search).isVisible = true
            if (viewModel.isUserAdmin()) findItem(R.id.menu_info).isVisible = true
        }
        return true
    }*/

    /*override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
    }*/

    override fun onDestroy() {
        //TextToSpeechUtil.onStop()
        super.onDestroy()
    }

    private fun performAfterDelay(delay: Long = 300L, callback: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            callback()
        }, delay)
    }

    fun onDrawerClick(item: SideNavItem) {
        /*val isUserLoggedIn = UserHelper.isLoggedIn()

        when (item.itemName) {
            getString(R.string.login_register) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    LoginActivity.startMe(this)
                }
            }

            getString(R.string.user) -> {
                // no-op
            }

            getString(R.string.saved) -> {
                if (!isUserLoggedIn) {
                    viewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = GlobalVar.CATEGORY_SAVED,
                        heading = getString(R.string.saved)
                    )
                }
            }

            getString(R.string.liked) -> {
                if (!isUserLoggedIn) {
                    viewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = GlobalVar.CATEGORY_USER_LIKED,
                        heading = getString(R.string.liked_words)
                    )
                }
            }

            getString(R.string.requested) -> {
                if (!isUserLoggedIn) {
                    viewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = GlobalVar.CATEGORY_USER_REQUESTED,
                        heading = getString(R.string.requested_words)
                    )
                }
            }

            getString(R.string.pending_approvals) -> {
                if (!isUserLoggedIn) {
                    viewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = GlobalVar.CATEGORY_PENDING_APPROVALS,
                        heading = getString(R.string.pending_approvals)
                    )
                }
            }

            getString(R.string.settings) -> {
                // no-op
            }

            getString(R.string.rate_us) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    TPUtils.navigateToPlayStore(
                        this,
                        getPackage()
                    )
                }
            }

            getString(R.string.tell_your_friend) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    val intent = Intent()
                    intent.action = Intent.ACTION_SEND
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        getString(R.string.msg_tell_your_friend)
                    )
                    intent.type = "text/plain"
                    startActivity(intent)
                }
            }

            getString(R.string.support) -> {
                // no-op
            }

            getString(R.string.help) -> {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    FAQActivity.startMe(this)
                }
                // no-op
            }

            getString(R.string.logout) -> {
                MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                    .showDialog(
                        title = getString(R.string.logout),
                        message = getString(R.string.msg_confirm_logout),
                        positiveText = getString(R.string.logout),
                        positiveListener = {
                            UserHelper.setUser(null)
                            LoginHelper.logoutUser()
                            PreferenceHelper.clear()
                            LoginActivity.startMe(this)
                            finish()
                        },
                        negativeListener = {}
                    )
            }
        }*/
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

    private fun setupObservers() {
//        viewModel.getUserMessage()
//            .observe(this, Observer {
//                TPUtils.showSnackBar(this, it)
//            })
//
//        viewModel.getChar()
//            .observe(this, Observer { it ->
//                it?.let {
//                    var c = binding.container.toolbarLayout.searchEt.text.toString()
//                    val start = binding.container.toolbarLayout.searchEt.selectionStart
//                    val end = binding.container.toolbarLayout.searchEt.selectionEnd
//                    when {
//                        start == end && start != 0 -> {
//                            c = c.substring(0, start) + it + c.substring(start, c.length)
//                            binding.container.toolbarLayout.searchEt.setText(c)
//                            binding.container.toolbarLayout.searchEt.setSelection(start + it.length)
//                        }
//
//                        start < end -> {
//                            c = c.replaceRange(startIndex = start, endIndex = end, it)
//                            binding.container.toolbarLayout.searchEt.setText(c)
//                            binding.container.toolbarLayout.searchEt.setSelection(start + it.length)
//                        }
//
//                        else -> {
//                            c += it
//                            binding.container.toolbarLayout.searchEt.setText(c)
//                            binding.container.toolbarLayout.searchEt.setSelection(c.length)
//                        }
//                    }
//                }
//            })
//
//        viewModel.getToolbarHeading()
//            .observe(this, Observer {
//                it?.let {
//                    binding.container.toolbarLayout.heading.text = it
//                }
//            })
    }

    private fun handleNavigation() {
//        navController.addOnDestinationChangedListener { _, destination, _ ->
//            when (destination.id) {
//                R.id.nav_home -> {
//                    homeNavigation()
//                }
//
//                R.id.nav_search -> {
//                    searchNavigation()
//                }
//
//                R.id.nav_wordList -> {
//                    wordListNavigation()
//                }
//            }
//        }
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
            heading.visibility = View.GONE
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
            heading.visibility = View.GONE
        }
        showKeyboard()
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun wordListNavigation() {
        showBackBtn = true
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.ic_arrow_back_white)
            setBackgroundDrawable(
                ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
            )
        }
        binding.container.toolbarLayout.apply {
            title.visibility = View.GONE
            searchToolbar.visibility = View.GONE
            heading.visibility = View.VISIBLE
        }
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    /*private fun navigateToWordListFragment(
        lang: String = GlobalVar.LANG_ANY,
        category: String,
        heading: String
    ) {
        viewModel.resetWordListParams()
        val param = WordListFragment.WordListFragmentParam(
            lang = lang,
            category = category
        )
        viewModel.setWordListParam(param)
        viewModel.setToolbarHeading(heading)
        navController.navigate(R.id.action_HomeFragment_to_WordListFragment)
    }*/

    private fun setupSearchEditText() {

        /*binding.container.toolbarLayout.searchEt.apply {
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
        }*/
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
