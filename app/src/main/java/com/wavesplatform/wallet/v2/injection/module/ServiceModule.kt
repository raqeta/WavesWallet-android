package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.data.service.UpdateApiDataService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {

    @ContributesAndroidInjector
    internal abstract fun updateHistoryService(): UpdateApiDataService
}
