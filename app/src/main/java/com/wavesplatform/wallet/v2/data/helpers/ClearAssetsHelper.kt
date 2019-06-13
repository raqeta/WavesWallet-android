/*
 * Created by Eduard Zaydel on 3/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.helpers

import com.vicpin.krealmextensions.queryAll
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.sdk.model.response.AssetBalanceResponse
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.AssetBalanceStoreDb
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.v2.util.isGateway

class ClearAssetsHelper {
    companion object {
        fun clearUnimportantAssets(prefsUtil: PrefsUtil, assets: MutableList<AssetBalanceResponse>, fromAPI: Boolean = false): MutableList<AssetBalanceResponse> {
            return if (!prefsUtil.getValue(PrefsUtil.KEY_IS_CLEARED_ASSETS, false)) {
                if (assets.size == EnvironmentManager.defaultAssets.size) {
                    // new account or empty account - let's go next
                    if (fromAPI) {
                        // don't need to clear any more if after load assets from API list is general
                        prefsUtil.setValue(PrefsUtil.KEY_IS_CLEARED_ASSETS, true)
                    }
                    assets
                } else {
                    // already exists account with unimportant assets: need clear
                    prefsUtil.setValue(PrefsUtil.KEY_IS_CLEARED_ASSETS, true)
                    checkAndClear(assets)
                }
            } else {
                assets
            }
        }

        private fun checkAndClear(assets: MutableList<AssetBalanceResponse>): MutableList<AssetBalanceResponse> {
            // load config for assets
            val savedAssetPrefs = queryAll<AssetBalanceStoreDb>()
            val savedAssetPrefsMap = savedAssetPrefs.associateBy { it.assetId }

            // filter unimportant assets
            val allUnimportantAssets = assets.filter { asset ->
                !asset.isWaves() && !isGateway(asset.assetId) && !asset.isFavorite
                        && !asset.isMyWavesToken(WavesWallet.getAddress())
            }

            // filter general assets with zero balance
            val generalAssetsWithZeroBalance = assets.filter { asset ->
                isGateway(asset.assetId) && !asset.isWaves() && !asset.isFavorite && asset.balance == 0L
            }

            // merge two list, clear and save
            val allAssetsToClear = allUnimportantAssets
                    .plus(generalAssetsWithZeroBalance)

            allAssetsToClear.forEach {
                it.isHidden = true
                savedAssetPrefsMap[it.assetId]?.isHidden = true
            }

            AssetBalanceDb.convertToDb(allAssetsToClear).saveAll()
            savedAssetPrefs.saveAll()

            return AssetBalanceDb.convertFromDb(queryAll()).toMutableList()
        }
    }
}