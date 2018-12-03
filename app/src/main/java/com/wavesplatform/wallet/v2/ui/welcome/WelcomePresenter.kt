package com.wavesplatform.wallet.v2.ui.welcome

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class WelcomePresenter @Inject constructor() : BasePresenter<WelcomeView>() {

    fun saveLanguage(lang: String) {
        preferenceHelper.setLanguage(lang)
    }
}
