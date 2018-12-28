package com.wavesplatform.wallet.v2.data.model.remote.response.history

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetInfo
import com.wavesplatform.wallet.v2.data.model.remote.response.Lease
import com.wavesplatform.wallet.v2.data.model.remote.response.Order
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
class Transaction : Parcelable {

    @SerializedName("__type")
    var type: String = ""
    @SerializedName("data")
    var data: Data? = null

    @Parcelize
    class Data : Parcelable {

        @SerializedName("height")
        var height: Long = 0
        @SerializedName("type")
        var type: Int = 0
        @SerializedName("id")
        var id: String = ""
        @SerializedName("timestamp")
        var timestamp: Date? = null
        @SerializedName("proofs")
        var proofs = arrayOf("")
        @SerializedName("version")
        var version: Int = 0
        @SerializedName("fee")
        var fee: String = ""
        @SerializedName("sender")
        var sender: String = ""
        @SerializedName("senderPublicKey")
        var senderPublicKey: String = ""
        @SerializedName("assetId")
        var assetId: String = ""
        @SerializedName("amount")
        var amount: Double = 0.0
        @SerializedName("signature")
        var signature: String = ""
        @SerializedName("recipient")
        var recipient: String = ""
        @SerializedName("recipientAddress")
        var recipientAddress: String? = ""
        @SerializedName("leaseId")
        var leaseId: String? = ""
        @SerializedName("alias")
        var alias: String? = ""
        @SerializedName("attachment")
        var attachment: String? = ""
        @SerializedName("status")
        var status: String? = ""
        @SerializedName("lease")
        var lease: Lease? = Lease()
        @SerializedName("feeAssetId")
        var feeAssetId: String? = null
        @SerializedName("feeAssetObject")
        var feeAssetObject: AssetInfo? = AssetInfo()
        @SerializedName("quantity")
        var quantity: Long = 0
        @SerializedName("price")
        var price: Long = 0
        @SerializedName("reissuable")
        var reissuable: Boolean = false
        @SerializedName("buyMatcherFee")
        var buyMatcherFee: Long = 0
        @SerializedName("sellMatcherFee")
        var sellMatcherFee: Long = 0
        @SerializedName("order1")
        var order1: Order? = Order()
        @SerializedName("order2")
        var order2: Order? = Order()
        @SerializedName("totalAmount")
        var totalAmount: Long = 0
        @SerializedName("transfers")
        var transfers = listOf<Transfer>()
        @SerializedName("data")
        var data = listOf<com.wavesplatform.wallet.v2.data.model.remote.response.Data>()
        @SerializedName("isPending")
        var isPending: Boolean = false
        @SerializedName("script")
        var script: String? = ""
        @SerializedName("minSponsoredAssetFee")
        var minSponsoredAssetFee: String? = ""
        var transactionTypeId: Int = 0
        var asset: AssetInfo? = AssetInfo()
    }

    @Parcelize
    class Transfer : Parcelable {
        @SerializedName("recipient")
        var recipient: String = ""
        @SerializedName("recipientAddress")
        var recipientAddress: String? = ""
        @SerializedName("amount")
        var amount: Double = 0.0
    }
}