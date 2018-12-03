package com.wavesplatform.wallet.v2.util

import android.support.v7.widget.GridLayoutManager

internal object SpanLookupFactory {

    fun singleSpan(): SpanLookup {
        return object : SpanLookup {
            override val spanCount: Int
                get() = 1

            override fun getSpanIndex(itemPosition: Int): Int {
                return 0
            }

            override fun getSpanSize(itemPosition: Int): Int {
                return 1
            }
        }
    }

    fun gridLayoutSpanLookup(layoutManager: GridLayoutManager): SpanLookup {
        return object : SpanLookup {
            override val spanCount: Int
                get() = layoutManager.spanCount

            override fun getSpanIndex(itemPosition: Int): Int {
                return layoutManager.spanSizeLookup.getSpanIndex(itemPosition, spanCount)
            }

            override fun getSpanSize(itemPosition: Int): Int {
                return layoutManager.spanSizeLookup.getSpanSize(itemPosition)
            }
        }
    }

}

internal interface SpanLookup {

    /**
     * @return number of spans in a row
     */
    val spanCount: Int

    /**
     * @param itemPosition
     * @return start span for the item at the given adapterActiveAdapter position
     */
    fun getSpanIndex(itemPosition: Int): Int

    /**
     * @param itemPosition
     * @return number of spans the item at the given adapterActiveAdapter position occupies
     */
    fun getSpanSize(itemPosition: Int): Int

}