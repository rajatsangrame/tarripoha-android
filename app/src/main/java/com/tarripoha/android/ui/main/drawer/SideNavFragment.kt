package com.tarripoha.android.ui.main.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tarripoha.android.R
import com.tarripoha.android.databinding.FragmentSideNavBinding
import com.tarripoha.android.ui.main.MainActivity
import com.tarripoha.android.util.TPUtils
import com.tarripoha.android.util.helper.UserHelper

class SideNavFragment : Fragment() {

  private lateinit var binding: FragmentSideNavBinding
  private val sideNavAdapter: SideNavAdapter = SideNavAdapter { position, item ->
    onItemClick(position, item)
  }

  private fun onItemClick(position: Int, item: SideNavItem) {
    (activity as MainActivity).onDrawerClick(item)
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSideNavBinding
      .inflate(LayoutInflater.from(requireContext()), container, false)
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setUpViews()
  }

  private fun setUpViews() {
    binding.rvSideNavOptions.apply {
      layoutManager = LinearLayoutManager(requireContext())
      adapter = sideNavAdapter
    }
    sideNavAdapter.setNavItemsData(prepareNavItems())
    binding.loginLayout.setOnClickListener {
      (activity as MainActivity).onDrawerClick(SideNavItem(getString(R.string.login_register), -1))
    }
    binding.userLayout.setOnClickListener {
      (activity as MainActivity).onDrawerClick(SideNavItem(getString(R.string.user), -1))
    }
  }

  private fun prepareNavItems(): List<SideNavItem> {
    val isUserLoggedIn = UserHelper.isLoggedIn()
    val menuItemsList = ArrayList<SideNavItem>()
    menuItemsList.apply {
      add(SideNavItem(getString(R.string.saved), R.drawable.ic_save_black))
      add(SideNavItem(getString(R.string.settings), R.drawable.ic_add_grey))
      add(SideNavItem(getString(R.string.rate_us), R.drawable.ic_add_grey))
      add(SideNavItem(getString(R.string.share), R.drawable.ic_share_black))
    }
    // Maintain the order here
    if (isUserLoggedIn) {
      menuItemsList.add(SideNavItem(getString(R.string.logout), R.drawable.ic_add_grey))
      binding.userLayout.visibility = View.VISIBLE
      binding.loginLayout.visibility = View.GONE
    } else {
      binding.loginLayout.visibility = View.VISIBLE
      binding.userLayout.visibility = View.GONE
    }
    binding.versionTv.text =
      getString(R.string.version, TPUtils.getAppVersionName(requireContext()))
    return menuItemsList
  }
}
