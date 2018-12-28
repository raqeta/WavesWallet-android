package com.wavesplatform.wallet.v2.data.model.remote.response.history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.Data
import com.wavesplatform.wallet.v2.data.model.remote.response.Transfer
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.notNull
import io.realm.RealmList
import kotlinx.android.parcel.Parcelize

@Parcelize
class Transactions : Parcelable {

    @SerializedName("__type")
    var type: String = ""
    @SerializedName("lastCursor")
    var lastCursor: String = ""
    @SerializedName("data")
    var data = listOf<Transaction>()

    companion object {

        fun convert(transactionUtil: TransactionUtil, transactions: List<Transaction>):
                List<com.wavesplatform.wallet.v2.data.model.remote.response.Transaction> {
            val list = arrayListOf<com.wavesplatform.wallet.v2.data.model.remote.response.Transaction>()
            for (tr in transactions) {
                list.add(convert(transactionUtil, tr))
            }
            return list
        }

        fun convert(transactionUtil: TransactionUtil, transaction: Transaction): com.wavesplatform.wallet.v2.data.model.remote.response.Transaction {
            val t = com.wavesplatform.wallet.v2.data.model.remote.response.Transaction()
            transaction.data.notNull {
                t.type = it.type
                t.id = it.id
                t.sender = it.sender
                t.senderPublicKey = it.senderPublicKey
                t.timestamp = it.timestamp?.time ?: 0
                t.amount = MoneyUtil.getUnscaledValue(
                        it.amount.toBigDecimal().toPlainString(), 8)
                t.signature = it.signature
                t.recipient = it.recipient
                t.recipientAddress = it.recipientAddress
                t.assetId = it.assetId
                t.leaseId = it.leaseId
                t.alias = it.alias
                t.attachment = it.attachment
                t.status = it.status
                t.lease = it.lease
                t.fee = MoneyUtil.getUnscaledValue(
                        it.fee.toBigDecimal().toPlainString(), 8)
                t.feeAssetId = it.feeAssetId
                t.feeAssetObject = it.feeAssetObject
                t.quantity = it.quantity
                t.price = it.price
                t.height = it.height
                t.reissuable = it.reissuable
                t.buyMatcherFee = it.buyMatcherFee
                t.sellMatcherFee = it.sellMatcherFee
                t.order1 = it.order1
                t.order2 = it.order2
                t.totalAmount = MoneyUtil.getUnscaledValue(
                        it.totalAmount.toBigDecimal().toPlainString(), 8)
                val transfers: RealmList<Transfer> = RealmList()
                for (item in it.transfers) {
                    transfers.add(Transfer(
                            item.recipient,
                            item.recipientAddress,
                            MoneyUtil.getUnscaledValue(
                                    item.amount.toBigDecimal().toPlainString(),
                                    8)))
                }
                t.transfers = transfers
                val data: RealmList<Data> = RealmList()
                for (item in it.data) {
                    data.add(Data(item.key, item.type, item.value))
                }
                t.data = data
                t.isPending = it.isPending
                t.transactionTypeId = transactionUtil.getTransactionType(t)
                t.asset = it.asset
                t.script = it.script
                t.minSponsoredAssetFee = it.minSponsoredAssetFee
            }
            return t
        }
    }
}