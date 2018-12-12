package com.wavesplatform.wallet.v2.data.model.local

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction

class HistoryItem : MultiItemEntity {
    private var itemType: Int = 0
    var header: String = ""
    var data: Transaction = Transaction()

    constructor(itemType: Int, header: String) {
        this.itemType = itemType
        this.header = header
    }

    constructor(itemType: Int, transaction: Transaction) {
        this.itemType = itemType
        this.data = transaction
    }

    override fun getItemType(): Int {
        return itemType
    }

    companion object {
        const val TYPE_EMPTY = 0
        const val TYPE_HEADER = 1
        const val TYPE_DATA = 2
    }
}