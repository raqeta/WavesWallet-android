/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.arellomobile.mvp.InjectViewState
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.save
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.db.SpamAssetDb
import com.wavesplatform.wallet.v2.data.model.local.AssetBalanceMultiItemEntity
import com.wavesplatform.wallet.v2.data.model.local.WalletSectionItem
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import pers.victor.ext.app
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class AssetsPresenter @Inject constructor() : BasePresenter<AssetsView>() {
    var needToScroll: Boolean = false

    fun loadAssetsBalance(withApiUpdate: Boolean = true) {
        if (Wavesplatform.getWallet() == null) {
            runOnUiThread {
                viewState.afterFailedLoadAssets()
            }
            return
        }

        viewState.startServiceToLoadData()
        runAsync {
            val savedAssetPrefs = queryAll<AssetBalanceStore>()
            var dbAssets = mutableListOf<AssetBalanceDb>()
            addSubscription(queryAllAsSingle<AssetBalanceDb>().toObservable()
                    .subscribeOn(Schedulers.io())
                    .map {
                        val assetBalanceList = it.toMutableList()
                        for (item in assetBalanceList) {
                            val assetBalance = savedAssetPrefs.firstOrNull { it.assetId == item.assetId }
                            assetBalance.notNull { storedAssetBalance ->
                                item.isFavorite = storedAssetBalance.isFavorite
                                item.position = storedAssetBalance.position
                                item.isHidden = storedAssetBalance.isHidden
                                item.save()
                            }
                        }
                        dbAssets = assetBalanceList
                        return@map createTripleSortedLists(assetBalanceList)
                    }
                    .doOnNext { postSuccess(it, withApiUpdate, true) }
                    .flatMap { tryUpdateWithApi(withApiUpdate, AssetBalanceDb.convertFromDb(dbAssets)) }
                    .map {
                        // update settings of spam list and send event to update
                        if (prefsUtil.getValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, false)) {
                            rxEventBus.post(Events.SpamFilterUrlChanged(true))
                        }
                        prefsUtil.setValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, false)
                        return@map it
                    }
                    .map { createTripleSortedLists(AssetBalanceDb.convertToDb(it)) }
                    .subscribe({
                        postSuccess(it, withApiUpdate, false)
                    }, {
                        it.printStackTrace()
                        runOnUiThread {
                            viewState.afterFailedLoadAssets()
                        }
                    }))
        }
    }

    fun reloadAssetsAfterSpamFilterStateChanged() {
        runAsync {
            addSubscription(Observable.zip(
                    queryAllAsSingle<AssetBalanceDb>().toObservable(),
                    queryAllAsSingle<SpamAssetDb>().toObservable()
                            .map { spamListFromDb ->
                                val enableSpamFilter = prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)
                                if (enableSpamFilter) {
                                    return@map listOf<SpamAssetDb>()
                                } else {
                                    return@map spamListFromDb
                                }
                            },
                    BiFunction { t1: List<AssetBalanceDb>, t2: List<SpamAssetDb> ->
                        return@BiFunction Pair(t1, t2)
                    })
                    .map { pairOfData ->
                        val assetsListFromDb = pairOfData.first
                        val spamListFromDb = pairOfData.second

                        assetsListFromDb.forEach { asset ->
                            asset.isSpam = spamListFromDb.any { it.assetId == asset.assetId }
                            if (assetsListFromDb.any { it.position != -1 }) {
                                if (asset.position == -1) {
                                    asset.position = assetsListFromDb.size + 1
                                }
                            }
                            if (asset.isSpam) {
                                asset.isFavorite = false
                            }
                        }

                        assetsListFromDb.saveAll()
                        AssetBalanceStore.saveAssetBalanceStore(AssetBalanceDb.convertFromDb(assetsListFromDb))
                        return@map assetsListFromDb
                    }
                    .map { createTripleSortedLists(it.toMutableList()) }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        postSuccess(it, false, true)
                    }, {
                        it.printStackTrace()
                        viewState.afterFailedUpdateAssets()
                    }))
        }
    }

    fun reloadAssetsAfterSpamUrlChanged() {
        prefsUtil.setValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, true)
        runAsync {
            addSubscription(nodeDataManager.loadSpamAssets()
                    .flatMap { newSpamAssets ->
                        Observable.zip(
                                queryAllAsSingle<AssetBalanceDb>().toObservable(),
                                Observable.just(SpamAssetDb.convertToDb(newSpamAssets)),
                                BiFunction { t1: List<AssetBalanceDb>, t2: List<SpamAssetDb> ->
                                    return@BiFunction Pair(t1, t2)
                                })
                    }
                    .map {
                        if (prefsUtil.getValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, false)) {
                            rxEventBus.post(Events.SpamFilterUrlChanged(true))
                        }
                        prefsUtil.setValue(PrefsUtil.KEY_NEED_UPDATE_TRANSACTION_AFTER_CHANGE_SPAM_SETTINGS, false)
                        return@map it
                    }
                    .map { pairOfData ->
                        val spamListFromDb = pairOfData.second
                        val assetsListFromDb = pairOfData.first

                        assetsListFromDb.forEach { asset ->
                            asset.isSpam = spamListFromDb.any { it.assetId == asset.assetId }
                            if (assetsListFromDb.any { it.position != -1 }) {
                                if (asset.position == -1) {
                                    asset.position = assetsListFromDb.size + 1
                                }
                            }
                            if (asset.isSpam) {
                                asset.isFavorite = false
                            }
                        }

                        assetsListFromDb.saveAll()
                        AssetBalanceStore.saveAssetBalanceStore(AssetBalanceDb.convertFromDb(assetsListFromDb))
                        return@map assetsListFromDb
                    }
                    .map { createTripleSortedLists(it.toMutableList()) }
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        postSuccess(it, false, true)
                    }, {
                        it.printStackTrace()
                        viewState.afterFailedUpdateAssets()
                    }))
        }
    }

    private fun tryUpdateWithApi(withApiUpdate: Boolean, it: List<AssetBalance>): Observable<List<AssetBalance>> {
        return if (withApiUpdate) {
            nodeDataManager.loadAssets(it)
        } else {
            Observable.just(it)
        }
    }

    private fun postSuccess(it: Triple<MutableList<AssetBalanceDb>, MutableList<AssetBalanceDb>,
            MutableList<AssetBalanceDb>>, withApiUpdate: Boolean, fromDb: Boolean) {
        val listToShow = arrayListOf<MultiItemEntity>()

        // add all main assets
        val assetBalances = mutableListOf<AssetBalanceMultiItemEntity>()
        it.first.forEach {
            assetBalances.add(AssetBalanceMultiItemEntity(it))
        }
        listToShow.addAll(assetBalances)

        // check if hidden assets exists and create section with them
        if (it.second.isNotEmpty()) {
            val hiddenSection = WalletSectionItem(app.getString(R.string.wallet_assets_hidden_category,
                    it.second.size.toString()))
            it.second.forEach {
                hiddenSection.addSubItem(it.convertFromDb())
            }
            listToShow.add(hiddenSection)
        }

        // check if spam assets exists and create section with them
        val enableSpamFilter = prefsUtil.getValue(PrefsUtil.KEY_ENABLE_SPAM_FILTER, true)
        if (!enableSpamFilter) {
            if (it.third.isNotEmpty()) {
                val spamSection = WalletSectionItem(app.getString(R.string.wallet_assets_spam_category,
                        it.third.size.toString()))
                it.third.forEach {
                    spamSection.addSubItem(it.convertFromDb())
                }
                listToShow.add(spamSection)
            }
        }

        // show all assets with sections
        runOnUiThread {
            viewState.afterSuccessLoadAssets(listToShow, fromDb, withApiUpdate)
        }
    }

    private fun createTripleSortedLists(list: MutableList<AssetBalanceDb>):
            Triple<MutableList<AssetBalanceDb>, MutableList<AssetBalanceDb>,
                    MutableList<AssetBalanceDb>> {
        val hiddenList = list
                .filter { it.isHidden && !it.isSpam }
                .sortedBy { it.position }
                .toMutableList()
        val sortedToFirstFavoriteList = list
                .asSequence()
                .filter { !it.isHidden && !it.isSpam }
                .sortedByDescending { it.isGateway }
                .sortedBy { it.position }
                .sortedByDescending { it.isFavorite }
                .toMutableList()
        val spamList = list
                .filter { it.isSpam }
                .toMutableList()
        return Triple(sortedToFirstFavoriteList, hiddenList, spamList)
    }

    fun loadAliases() {
        if (Wavesplatform.isAuthenticated()) {
            addSubscription(apiDataManager.loadAliases()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe {})
        }
    }
}
