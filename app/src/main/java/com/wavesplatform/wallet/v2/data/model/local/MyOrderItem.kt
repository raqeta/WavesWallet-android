package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.SectionEntity
import com.wavesplatform.wallet.v2.data.model.remote.response.OrderResponse

class MyOrderItem : SectionEntity<OrderResponse> {
    constructor(isHeader: Boolean, header: String) : super(isHeader, header)

    constructor(t: OrderResponse) : super(t)
}