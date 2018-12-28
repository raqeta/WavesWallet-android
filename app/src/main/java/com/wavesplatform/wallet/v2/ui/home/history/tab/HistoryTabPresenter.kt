package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAsSingle
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.data.model.local.LeasingStatus
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.data.model.remote.response.history.Transactions
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content.AssetDetailsContentPresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.TransactionUtil
import com.wavesplatform.wallet.v2.util.isWavesId
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@InjectViewState
class HistoryTabPresenter @Inject constructor() : BasePresenter<HistoryTabView>() {
    var allItemsFromDb = listOf<Transaction>()
    var totalHeaders = 0
    var type: String? = "all"
    var hashOfTimestamp = hashMapOf<Long, Long>()
    var assetBalance: AssetBalance? = null
    var lastCursor = ""

    @Inject
    lateinit var transactionUtil: TransactionUtil

    fun loadTransactions() {
        when (type) {
            HistoryTabFragment.all -> addSubscription(apiDataManager.loadTransactionsAll()
                    .toList().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        onSuccessLoad(it)
                    }, {
                        it.printStackTrace()
                    })
            )
            HistoryTabFragment.send -> addSubscription(apiDataManager.loadTransactionsSend()
                    .toList().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        onSuccessLoad(it)
                    }, {
                        it.printStackTrace()
                    })
            )
            HistoryTabFragment.exchanged -> addSubscription(apiDataManager.loadTransactionsExchange()
                    .toList().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        onSuccessLoad(it)
                    }, {
                        it.printStackTrace()
                    })
            )
            else -> {
                /*runAsync {
                addSubscription(loadFromDb()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ list ->
                            if (list.isEmpty()) {
                                // loadLastTransactions()
                            } else {
                                viewState.afterSuccessLoadTransaction(list, type)
                            }
                        }, {
                            viewState.onShowError(R.string.history_error_receive_data)
                            it.printStackTrace()
                        }))
            }*/
            }
        }
    }

    fun loadNextHistory() {
        when (type) {
            HistoryTabFragment.all -> addSubscription(apiDataManager.loadNextTransactionsAll(lastCursor)
                    .toList().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        onSuccessLoad(it)
                    }, {
                        it.printStackTrace()
                    })
            )
            HistoryTabFragment.send -> addSubscription(apiDataManager.loadNextTransactionsSend(lastCursor)
                    .toList().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        onSuccessLoad(it)
                    }, {
                        it.printStackTrace()
                    })
            )
            HistoryTabFragment.all -> addSubscription(apiDataManager.loadNextTransactionsExchange(lastCursor)
                    .toList().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        onSuccessLoad(it)
                    }, {
                        it.printStackTrace()
                    })
            )
        }
    }

    private fun onSuccessLoad(data: MutableList<Transactions>) {
        val transactions = Transactions.convert(transactionUtil, data[0].data)
        transactions.saveAll()
        val items = sortAndConfigToUi(true, transactions)
        lastCursor = data[0].lastCursor
        viewState.afterSuccessLoadAddTransaction(items, type)
    }

    private fun loadFromDb(): Single<ArrayList<HistoryItem>> {
        Log.d("historydev", "on presenter")
        val singleData: Single<List<Transaction>> = when (type) {
            HistoryTabFragment.all -> {
                queryAllAsSingle()
            }
            HistoryTabFragment.exchanged -> {
                queryAsSingle { `in`("transactionTypeId", arrayOf(Constants.ID_EXCHANGE_TYPE)) }
            }
            HistoryTabFragment.issued -> {
                queryAsSingle {
                    `in`("transactionTypeId", arrayOf(Constants.ID_TOKEN_REISSUE_TYPE,
                            Constants.ID_TOKEN_BURN_TYPE, Constants.ID_TOKEN_GENERATION_TYPE))
                }
            }
            HistoryTabFragment.leased -> {
                queryAsSingle {
                    `in`("transactionTypeId", arrayOf(Constants.ID_INCOMING_LEASING_TYPE,
                            Constants.ID_CANCELED_LEASING_TYPE, Constants.ID_STARTED_LEASING_TYPE))
                }
            }
            HistoryTabFragment.send -> {
                queryAsSingle {
                    `in`("transactionTypeId", arrayOf(Constants.ID_SENT_TYPE, Constants.ID_MASS_SEND_TYPE))
                }
            }
            HistoryTabFragment.received -> {
                queryAsSingle {
                    `in`("transactionTypeId", arrayOf(Constants.ID_RECEIVED_TYPE, Constants.ID_MASS_RECEIVE_TYPE,
                            Constants.ID_MASS_SPAM_RECEIVE_TYPE, Constants.ID_SPAM_RECEIVE_TYPE))
                }
            }
            HistoryTabFragment.leasing_all -> {
                queryAsSingle {
                    `in`("transactionTypeId", arrayOf(Constants.ID_STARTED_LEASING_TYPE,
                            Constants.ID_INCOMING_LEASING_TYPE, Constants.ID_CANCELED_LEASING_TYPE))
                }
            }
            HistoryTabFragment.leasing_active_now -> {
                queryAsSingle {
                    equalTo("status", LeasingStatus.ACTIVE.status)
                            .and()
                            .equalTo("transactionTypeId", Constants.ID_STARTED_LEASING_TYPE)
                }
            }
            HistoryTabFragment.leasing_canceled -> {
                queryAsSingle {
                    `in`("transactionTypeId", arrayOf(Constants.ID_CANCELED_LEASING_TYPE))
                }
            }
            else -> {
                queryAllAsSingle()
            }
        }

        return singleData.map { transitions ->
            allItemsFromDb = if (assetBalance == null) {
                transitions.sortedByDescending { transaction -> transaction.timestamp }
            } else {
                filterDetailed(transitions, assetBalance!!.assetId)
                        .sortedByDescending { transaction -> transaction.timestamp }
            }
            return@map sortAndConfigToUi(true, allItemsFromDb)
        }
    }

    private fun filterDetailed(transactions: List<Transaction>, assetId: String): List<Transaction> {
        return transactions.filter { transaction ->
            assetId.isWavesId() && transaction.assetId.isNullOrEmpty()
                    || AssetDetailsContentPresenter.isAssetIdInExchange(transaction, assetId)
                    || transaction.assetId == assetId
        }
    }

    private fun sortAndConfigToUi(needInit: Boolean, it: List<Transaction>): ArrayList<HistoryItem> {
        if (needInit) {
            init()
        }

        val dateFormat = SimpleDateFormat("MMMM dd, yyyy",
                Language.getLocale(preferenceHelper.getLanguage()))

        val sortedList = it
                .mapTo(mutableListOf()) {
                    HistoryItem(HistoryItem.TYPE_DATA, it)
                }

        val list = arrayListOf<HistoryItem>()

        sortedList.forEach {
            val startDate = Calendar.getInstance()
            startDate.timeInMillis = it.data.timestamp
            startDate.set(Calendar.HOUR_OF_DAY, 0)
            startDate.set(Calendar.MINUTE, 0)
            startDate.set(Calendar.SECOND, 0)
            startDate.set(Calendar.MILLISECOND, 0)
            val timestamp = startDate.timeInMillis
            if (hashOfTimestamp[timestamp] == null) {
                hashOfTimestamp[timestamp] = timestamp
                list.add(HistoryItem(HistoryItem.TYPE_HEADER,
                        dateFormat.format(Date(it.data.timestamp)).capitalize()))
                totalHeaders++
            }
            list.add(it)
        }

        if (list.isNotEmpty()) {
            list.add(0, HistoryItem(HistoryItem.TYPE_EMPTY, ""))
        }

        return list
    }

    private fun init() {
        totalHeaders = 0
        hashOfTimestamp = hashMapOf()
    }

}
