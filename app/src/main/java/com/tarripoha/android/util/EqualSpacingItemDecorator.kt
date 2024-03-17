package com.tarripoha.android.util

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO Add support for Linear Layout with vertical orientation
class EqualSpacingItemDecorator(
    var spanCount: Int,
    @Px var spacing: Int,
    var includeEdge: Boolean
) :
    RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        when (parent.layoutManager) {

            is GridLayoutManager -> {

                val position = parent.getChildAdapterPosition(view) // item position
                val column: Int = position % spanCount // item column

                if (includeEdge) {
                    outRect.left =
                        spacing - column * spacing / spanCount
                    outRect.right =
                        (column + 1) * spacing / spanCount
                    if (position < spanCount) { // top edge
                        outRect.top = spacing
                    }
                    outRect.bottom = spacing // item bottom
                } else {
                    outRect.left =
                        column * spacing / spanCount
                    outRect.right =
                        spacing - (column + 1) * spacing / spanCount
                    if (position >= spanCount) {
                        outRect.top = spacing // item top
                    }
                }
            }

            is LinearLayoutManager -> {
                outRect.left = spacing
                outRect.top = spacing
                outRect.bottom = spacing
                parent.itemDecorationCount
                if (parent.getChildAdapterPosition(view) == parent.adapter?.itemCount!! - 1
                ) {
                    outRect.right = spacing
                } else {
                    outRect.right = 0;
                }

            }

            else -> super.getItemOffsets(outRect, view, parent, state)
        }


    }
}