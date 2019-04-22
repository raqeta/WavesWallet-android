/*
 * Created by Eduard Zaydel on 15/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.response

import com.google.gson.annotations.SerializedName

data class LastAppVersion(
        @SerializedName("last_version")
        var lastVersion: String
)