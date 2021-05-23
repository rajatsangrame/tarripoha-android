package com.tarripoha.android.ui.main.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tarripoha.android.R
import com.tarripoha.android.databinding.FragmentSideNavBinding
import com.tarripoha.android.ui.main.MainActivity
import com.tarripoha.android.util.helper.UserHelper

class SideNavFragment : Fragment() {

  private lateinit var binding: FragmentSideNavBinding
  private val sideNavAdapter: SideNavAdapter = SideNavAdapter { position, item ->
    onItemClick(position, item)
  }

  private fun onItemClick(position: Int, item: SideNavItem) {
    Toast.makeText(requireContext(), "" + item.itemName, Toast.LENGTH_LONG).show()
    (activity as MainActivity).closeDrawer(item)
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
  }

  private fun prepareNavItems(): List<SideNavItem> {
    val isUserLoggedIn = UserHelper.isLoggedIn()
    val menuItemsList = ArrayList<SideNavItem>()
    menuItemsList.apply {
      add(SideNavItem(1, "Saved", R.drawable.ic_add_grey))
      add(SideNavItem(2, "Rate Us", R.drawable.ic_add_grey))
      add(SideNavItem(3, "Share", R.drawable.ic_add_grey))
      add(SideNavItem(5, "Settings", R.drawable.ic_add_grey))
    }
    if (!isUserLoggedIn) {
      menuItemsList.add(SideNavItem(5, "Logout", R.drawable.ic_add_grey))
    }
    return menuItemsList
  }
}
