package com.tarripoha.android.presentation.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.core.view.GravityCompat
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.tarripoha.android.R
import com.tarripoha.android.databinding.ActivityMainBinding
import com.tarripoha.android.presentation.base.BaseActivity
import com.tarripoha.android.presentation.login.LoginActivity
import com.tarripoha.android.presentation.main.drawer.SideNavItem
import com.tarripoha.android.util.*
import com.tarripoha.android.Constants
import com.tarripoha.android.util.helper.UserHelper
import com.tarripoha.android.util.ktx.getPackage
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var navController: NavController
    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

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
            if (mainViewModel.isUserAdmin()) findItem(R.id.menu_info).isVisible = true
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
                val count = if (mainViewModel.getWordCount().value == null) {
                    0
                } else {
                    mainViewModel.getWordCount().value
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

    fun openDrawer() {
        binding.drawerLayout.open()
    }

    fun onDrawerClick(item: SideNavItem) {
        val isUserLoggedIn = UserHelper.isLoggedIn()

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
                    mainViewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = Constants.CATEGORY_SAVED,
                        heading = getString(R.string.saved)
                    )
                }
            }

            getString(R.string.liked) -> {
                if (!isUserLoggedIn) {
                    mainViewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = Constants.CATEGORY_USER_LIKED,
                        heading = getString(R.string.liked_words)
                    )
                }
            }

            getString(R.string.requested) -> {
                if (!isUserLoggedIn) {
                    mainViewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = Constants.CATEGORY_USER_REQUESTED,
                        heading = getString(R.string.requested_words)
                    )
                }
            }

            getString(R.string.pending_approvals) -> {
                if (!isUserLoggedIn) {
                    mainViewModel.setUserMessage(getString(R.string.error_login))
                    return
                }
                binding.drawerLayout.closeDrawer(GravityCompat.START)
                performAfterDelay {
                    navigateToWordListFragment(
                        category = Constants.CATEGORY_PENDING_APPROVALS,
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
                    //FAQActivity.startMe(this)
                }
                // no-op
            }

            getString(R.string.logout) -> {
//                MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
//                    .showDialog(
//                        title = getString(R.string.logout),
//                        message = getString(R.string.msg_confirm_logout),
//                        positiveText = getString(R.string.logout),
//                        positiveListener = {
//                            UserHelper.setUser(null)
//                            LoginHelper.logoutUser()
//                            PreferenceHelper.clear()
//                            LoginActivity.startMe(this)
//                            finish()
//                        },
//                        negativeListener = {}
//                    )
            }
        }
    }

    //endregion

    //region Helper functions

    private fun setupUI() {
        setupObservers()
        setupListeners()
        setupSearchEditText()
        navController = findNavController(R.id.nav_host_fragment)
        navController.setGraph(R.navigation.main_nav_graph)
    }

    private fun setupObservers() {
        mainViewModel.getUserMessage()
            .observe(this, Observer {
                it?.let {
                    TPUtils.showSnackBar(this, it)
                }
            })

        mainViewModel.getErrorMessage()
            .observe(this, Observer {
                it?.let {
                    TPUtils.showSnackBar(this, it)
                }
            })
//
//        mainViewModel.getChar()
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
    }

    private fun navigateToWordListFragment(
        lang: String = Constants.LANG_ANY,
        category: String,
        heading: String
    ) {
        mainViewModel.resetWordListParams()
        val param = WordListFragment.WordListFragmentParam(
            heading = heading,
            lang = lang,
            category = category
        )
        mainViewModel.wordListParam = param
        navController.navigate(R.id.action_HomeFragment_to_WordListFragment)
    }

    private fun setupSearchEditText() {

        /*binding.container.toolbarLayout.searchEt.apply {
            val d = RxTextView.textChanges(this)
                .subscribeOn(AndroidSchedulers.mainThread())
                .debounce(SEARCH_DEBOUNCE_TIME_IN_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    mainViewModel.setQuery(it.toString())
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
    }

    //endregion

}
