package com.wavesplatform.wallet.v2.util

import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import javax.inject.Inject

/**
 * Created by anonymous on 07.03.18.
 */

class TransactionUtil @Inject constructor() {
    fun getTransactionType(transaction: Transaction): Int =
            if (transaction.type == Transaction.TRANSFER
                    && transaction.sender != App.getAccessManager().getWallet()?.address
                    && transaction.asset?.isSpam == true) {
                Constants.ID_SPAM_RECEIVE_TYPE
            } else if (transaction.type == Transaction.MASS_TRANSFER
                    && transaction.sender != App.getAccessManager().getWallet()?.address
                    && transaction.asset?.isSpam == true) {
                Constants.ID_MASS_SPAM_RECEIVE_TYPE
            } else if (transaction.type == Transaction.LEASE_CANCEL
                    && !transaction.leaseId.isNullOrEmpty()) {
                Constants.ID_CANCELED_LEASING_TYPE
            } else if ((transaction.type == Transaction.TRANSFER || transaction.type == 9)
                    && transaction.sender != App.getAccessManager().getWallet()?.address) {
                Constants.ID_RECEIVED_TYPE
            } else if (transaction.type == Transaction.TRANSFER
                    && transaction.sender == transaction.recipientAddress) {
                Constants.ID_SELF_TRANSFER_TYPE
            } else if (transaction.type == Transaction.TRANSFER
                    && transaction.sender == App.getAccessManager().getWallet()?.address) {
                Constants.ID_SENT_TYPE
            } else if (transaction.type == Transaction.LEASE
                    && transaction.recipientAddress != App.getAccessManager().getWallet()?.address) {
                Constants.ID_STARTED_LEASING_TYPE
            } else if (transaction.type == Transaction.EXCHANGE) {
                Constants.ID_EXCHANGE_TYPE
            } else if (transaction.type == Transaction.ISSUE) {
                Constants.ID_TOKEN_GENERATION_TYPE
            } else if (transaction.type == Transaction.BURN) {
                Constants.ID_TOKEN_BURN_TYPE
            } else if (transaction.type == Transaction.REISSUE) {
                Constants.ID_TOKEN_REISSUE_TYPE
            } else if (transaction.type == Transaction.CREATE_ALIAS) {
                Constants.ID_CREATE_ALIAS_TYPE
            } else if (transaction.type == Transaction.LEASE
                    && transaction.recipientAddress == App.getAccessManager().getWallet()?.address) {
                Constants.ID_INCOMING_LEASING_TYPE
            } else if (transaction.type == Transaction.MASS_TRANSFER
                    && transaction.sender == App.getAccessManager().getWallet()?.address) {
                Constants.ID_MASS_SEND_TYPE
            } else if (transaction.type == Transaction.MASS_TRANSFER
                    && transaction.sender != App.getAccessManager().getWallet()?.address) {
                Constants.ID_MASS_RECEIVE_TYPE
            } else if (transaction.type == Transaction.DATA) {
                Constants.ID_DATA_TYPE
            } else if (transaction.type == Transaction.SCRIPT) {
                if (transaction.script == null) {
                    Constants.ID_CANCEL_SCRIPT_TYPE
                } else {
                    Constants.ID_SET_SCRIPT_TYPE
                }
            } else if (transaction.type == Transaction.SPONSORSHIP) {
                if (transaction.minSponsoredAssetFee == null) {
                    Constants.ID_CANCEL_SPONSORSHIP_TYPE
                } else {
                    Constants.ID_SET_SPONSORSHIP_TYPE
                }
            }
            else {
                Constants.ID_UNRECOGNISED_TYPE
            }
}