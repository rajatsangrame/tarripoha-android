package com.tarripoha.android.ui.main.drawer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.tarripoha.android.databinding.LayoutItemSideNavBinding

internal class SideNavAdapter(private val onItemClick: ((position: Int, item: SideNavItem) -> Unit)) :
    RecyclerView.Adapter<SideNavAdapter.SideNavVH>() {
    private var menuItemsList = ArrayList<SideNavItem>()

    fun setNavItemsData(list: List<SideNavItem>) {
        menuItemsList.clear()
        menuItemsList.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SideNavAdapter.SideNavVH {
        val binding = LayoutItemSideNavBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return SideNavVH(binding)
    }

    inner class SideNavVH(val binding: LayoutItemSideNavBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun setData(sideNavItem: SideNavItem) {
            itemView.setOnClickListener {
                sideNavItem.let {
                    onItemClick.invoke(adapterPosition, sideNavItem)
                }
            }
            binding.ivNavOption.setImageResource(sideNavItem.resourceId)
            binding.tvNavText.text = sideNavItem.itemName
            sideNavItem.resourceTint?.let {
                binding.ivNavOption.setColorFilter(ContextCompat.getColor(itemView.context, it))
                binding.tvNavText.setTextColor(ContextCompat.getColor(itemView.context, it))
            }
        }
    }

    override fun getItemCount(): Int {
        return menuItemsList.size
    }

    override fun onBindViewHolder(holder: SideNavVH, position: Int) {
        holder.setData(menuItemsList[position])
    }
}
