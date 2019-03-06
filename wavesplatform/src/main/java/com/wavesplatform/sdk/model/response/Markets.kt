package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName

data class Markets(
    @SerializedName("matcherPublicKey") var matcherPublicKey: String = "",
    @SerializedName("markets") var markets: List<MarketResponse> = listOf()
)