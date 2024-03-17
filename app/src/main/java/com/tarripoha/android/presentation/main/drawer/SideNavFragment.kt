package com.tarripoha.android.presentation.main.drawer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.tarripoha.android.R
import com.tarripoha.android.databinding.FragmentSideNavBinding
import com.tarripoha.android.presentation.main.MainActivity
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
            (activity as MainActivity).onDrawerClick(
                SideNavItem(
                    getString(R.string.login_register),
                    -1
                )
            )
        }
        binding.userLayout.setOnClickListener {
            (activity as MainActivity).onDrawerClick(SideNavItem(getString(R.string.user), -1))
        }
    }

    private fun prepareNavItems(): List<SideNavItem> {
        val isUserLoggedIn = UserHelper.isLoggedIn()
        val isUserAdmin: Boolean = UserHelper.getUser()?.admin ?: false
        val menuItemsList = ArrayList<SideNavItem>()
        menuItemsList.apply {
            add(SideNavItem(getString(R.string.liked), R.drawable.ic_like_grey))
            add(SideNavItem(getString(R.string.requested), R.drawable.ic_pending))
            add(SideNavItem(getString(R.string.saved), R.drawable.ic_save_black))
            add(SideNavItem(getString(R.string.settings), R.drawable.ic_settings_grey))
            add(SideNavItem(getString(R.string.rate_us), R.drawable.ic_star_grey))
            add(SideNavItem(getString(R.string.tell_your_friend), R.drawable.ic_whatsapp_black))
            add(SideNavItem(getString(R.string.support), R.drawable.ic_donate_grey))
            add(SideNavItem(getString(R.string.help), R.drawable.ic_help_black))
        }
        // Maintain the order here
        if (isUserLoggedIn) {
            if (isUserAdmin) {
                menuItemsList.add(
                    SideNavItem(
                        getString(R.string.pending_approvals),
                        R.drawable.ic_flag_black
                    )
                )
            }
            menuItemsList.add(
                SideNavItem(
                    getString(R.string.logout),
                    R.drawable.ic_logout_grey,
                    R.color.colorRed
                )
            )
            binding.apply {
                userLayout.visibility = View.VISIBLE
                loginLayout.visibility = View.GONE
                nameTv.text = UserHelper.getName()
                phoneTv.text = UserHelper.getPhone()
            }
        } else {
            binding.loginLayout.visibility = View.VISIBLE
            binding.userLayout.visibility = View.GONE
        }
        binding.versionTv.text =
            getString(R.string.version, TPUtils.getAppVersionName(requireContext()))
        return menuItemsList
    }
}
