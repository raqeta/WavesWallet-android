package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.request.CreateLeasingRequest
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.makeAsAlias
import javax.inject.Inject

@InjectViewState
class ConfirmationStartLeasingPresenter @Inject constructor() : BasePresenter<ConfirmationStartLeasingView>() {

    var createLeasingRequest: CreateLeasingRequest = CreateLeasingRequest()
    var recipientIsAlias = false
    var address: String = ""
    var amount: String = ""


    fun startLeasing() {
        if (recipientIsAlias) {
            createLeasingRequest.recipient = address.makeAsAlias()
        } else {
            createLeasingRequest.recipient = address
        }
        createLeasingRequest.amount = MoneyUtil.getUnscaledValue(amount, 8)

        addSubscription(nodeDataManager.startLeasing(createLeasingRequest, recipientIsAlias)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successStartLeasing()
                    viewState.showProgressBar(false)
                }, {
                    viewState.failedStartLeasing()
                    viewState.showProgressBar(false)
                    it.printStackTrace()
                }))
    }
}
