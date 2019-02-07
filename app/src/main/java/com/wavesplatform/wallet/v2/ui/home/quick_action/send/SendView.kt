package com.wavesplatform.wallet.v2.ui.home.quick_action.send

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.XRate
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SendView : BaseMvpView {

    fun onShowError(errorMsgRes: Int)
    fun onShowPaymentDetails()
    fun showXRate(xRate: XRate, ticker: String)
    fun showXRateError()
    fun setRecipientValid(valid: Boolean?)
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
    fun showLoadAssetSuccess(assetBalance: AssetBalance)
    fun showLoadAssetError(errorMsgRes: Int)
}
