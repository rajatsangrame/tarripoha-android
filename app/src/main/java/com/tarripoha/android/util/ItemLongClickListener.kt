package com.tarripoha.android.util

/**
 * Generic item click listener for item of RecyclerView.
 */
interface ItemLongClickListener<T> {
  /**
   * Called when item is clicked in adapter's list.
   *
   * @param position Position of clicked item.
   * @param data Data of clicked item.
   */
  fun onClick(
    position: Int,
    data: T
  )
}
