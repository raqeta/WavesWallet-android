package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface CreatePasscodeView : BaseMvpView {
    fun onSuccessCreatePassCode(guid: String, passCode: String)
    fun onFailCreatePassCode()
}
