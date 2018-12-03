package com.wavesplatform.wallet.v2.ui.home.profile.addresses

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AddressesAndKeysPresenter @Inject constructor() : BasePresenter<AddressesAndKeysView>() {

    fun loadAliases() {
        runAsync {
            addSubscription(
                    queryAllAsSingle<Alias>().toObservable()
                            .observeOn(AndroidSchedulers.mainThread())
                            .map { aliases ->
                                val ownAliases = aliases.filter { it.own }
                                runOnUiThread { viewState.afterSuccessLoadAliases(ownAliases) }
                            }
                            .observeOn(Schedulers.io())
                            .flatMap {
                                apiDataManager.loadAliases()
                            }
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe {
                                runOnUiThread { viewState.afterSuccessLoadAliases(it) }
                            })
        }
    }
}
