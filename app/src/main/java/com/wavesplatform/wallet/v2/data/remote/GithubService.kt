/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalTransactionCommission
import com.wavesplatform.wallet.v2.data.model.remote.response.LastAppVersion
import com.wavesplatform.wallet.v2.data.model.remote.response.News
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface GithubService {

    @GET
    fun spamAssets(@Url url: String = EnvironmentManager.servers.spamUrl): Observable<String>

    @GET
    fun news(@Url url: String = News.URL): Observable<News>

    @GET
    fun globalConfiguration(@Url url: String = EnvironmentManager.environment.url): Observable<GlobalConfiguration>

    @GET
    fun globalCommission(@Url url: String = EnvironmentManager.URL_COMMISSION_MAIN_NET): Observable<GlobalTransactionCommission>

    @GET
    fun loadLastAppVersion(@Url url: String = Constants.URL_VERSION): Observable<LastAppVersion>
}
