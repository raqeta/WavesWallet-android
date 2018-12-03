package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.remote.response.LastTrade
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookAdapter

class LastPriceItem(var spreadPercent: Double?, item: LastTrade) : MultiItemEntity {
    var lastTrade: LastTrade? = item


    override fun getItemType(): Int {
        return TradeOrderBookAdapter.LAST_PRICE_TYPE
    }
}