package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem

@StateStrategyType(SkipStrategy::class)
interface AssetDetailsContentView : BaseMvpView {
    fun showLastTransactions(data: MutableList<HistoryItem>)
}
