package com.wavesplatform.wallet.v1.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wavesplatform.wallet.v1.crypto.PublicKeyAccount;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.payload.AssetBalances;
import com.wavesplatform.wallet.v1.payload.ExchangeTransaction;
import com.wavesplatform.wallet.v1.payload.IssueTransaction;
import com.wavesplatform.wallet.v1.payload.MassTransferTransaction;
import com.wavesplatform.wallet.v1.payload.PaymentTransaction;
import com.wavesplatform.wallet.v1.payload.ReissueTransaction;
import com.wavesplatform.wallet.v1.payload.Transaction;
import com.wavesplatform.wallet.v1.payload.TransactionsInfo;
import com.wavesplatform.wallet.v1.payload.TransferTransaction;
import com.wavesplatform.wallet.v1.request.IssueTransactionRequest;
import com.wavesplatform.wallet.v1.request.ReissueTransactionRequest;
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class NodeManager {
    private static NodeManager instance;
    private final NodeApi service;

    public static NodeManager get() {
        return instance;
    }

    public static NodeManager createInstance(String pubKey) {
        try {
            instance = new NodeManager(pubKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    public AssetBalances assetBalances = new AssetBalances();
    public List<Transaction> transactions = new ArrayList<>();
    public List<Transaction> pendingTransactions = new ArrayList<>();
    public List<AssetBalance> pendingAssets = new ArrayList<>();

    public String getAddress() {
        return publicKeyAccount.getAddress();
    }
    private final Gson gson;
    private final PublicKeyAccount publicKeyAccount;

    private NodeManager(String pubKey) throws PublicKeyAccount.InvalidPublicKey {
        final RuntimeTypeAdapterFactory<Transaction> typeFactory = RuntimeTypeAdapterFactory
                .of(Transaction.class, "type")
                .registerSubtype(PaymentTransaction.class, "2")
                .registerSubtype(IssueTransaction.class, "3")
                .registerSubtype(TransferTransaction.class, "4")
                .registerSubtype(ReissueTransaction.class, "5")
                .registerSubtype(ExchangeTransaction.class, "7")
                .registerSubtype(MassTransferTransaction.class, "11")
                .registerDefaultSubtype(Transaction.class, "0");

        gson = new GsonBuilder().registerTypeAdapterFactory(typeFactory).create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(EnvironmentManager.get().current().getNodeUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
        service = retrofit.create(NodeApi.class);

        this.publicKeyAccount = new PublicKeyAccount(pubKey);
    }

    public AssetBalance wavesAsset = new AssetBalance() {{
        assetId = null;
        quantity = 100000000L * 100000000L;
        issueTransaction = new IssueTransaction();
        issueTransaction.decimals = 8;
        issueTransaction.quantity = quantity;
        issueTransaction.name = "WAVES";
    }};

    private List<Transaction> filterOwnTransactions(List<Transaction> txs) {
        List<Transaction> own = new ArrayList<>();
        for (Transaction tx : txs) {
            if (tx.isOwn()) {
                tx.isPending = true;
                own.add(tx);
            }
        }

        return own;
    }

    public Completable loadBalancesAndTransactions() {
        return Observable.zip(service.wavesBalance(getAddress()),
                service.assetsBalance(getAddress()),
                service.transactionList(getAddress(), 50).map(r -> r.get(0)),
                service.unconfirmedTransactions(),
                (bal, abs, txs, pending) -> {
                    wavesAsset.balance = bal.balance;
                    this.assetBalances = abs;
                    Collections.sort(this.assetBalances.balances, (o1, o2) -> o1.assetId.compareTo(o2.assetId));
                    this.assetBalances.balances.add(0, wavesAsset);
                    this.pendingTransactions = filterOwnTransactions(pending);
                    this.transactions = txs;

                    updatePendingTxs();
                    updatePendingBalances();

                    return Pair.of(abs, txs);
                }).ignoreElements();
    }

    private void updatePendingTxs() {
        for (Transaction tx : this.transactions) {
            Iterator<Transaction> i = pendingTransactions.iterator();
            while (i.hasNext()) {
                if (tx.id.equals(i.next().id)) {
                    i.remove();
                }
            }
        }
    }

    private void updatePendingBalances() {
        for (AssetBalance ab : this.assetBalances.balances) {
            Iterator<AssetBalance> i = pendingAssets.iterator();
            while (i.hasNext()) {
                if (ab.isAssetId(i.next().assetId)) {
                    i.remove();
                }
            }
        }
    }

    public Observable<TransactionsInfo> getTransactionsInfo(final String asset) {
        return service.getTransactionsInfo(asset);
    }

    public List<AssetBalance> getAllAssets() {
        List<AssetBalance> all = new ArrayList<>();
        all.addAll(pendingAssets);
        all.addAll(assetBalances.balances);
        return all;
    }

    public String getAssetName(String assetId) {
        return assetBalances.getAssetName(assetId);
    }

    public String getPublicKeyStr() {
        return publicKeyAccount.getPublicKeyStr();
    }

    public Observable<TransferTransactionRequest> broadcastTransfer(TransferTransactionRequest tx) {
        return service.broadcastTransfer(tx);
    }

    public Observable<TransactionsBroadcastRequest> transactionsBroadcast(TransactionsBroadcastRequest tx) {
        return service.transactionsBroadcast(tx);
    }

    public Observable<IssueTransactionRequest> broadcastIssue(IssueTransactionRequest tx) {
        return service.broadcastIssue(tx);
    }

    public Observable<ReissueTransactionRequest> broadcastReissue(ReissueTransactionRequest tx) {
        return service.broadcastReissue(tx);
    }

    public AssetBalance getAssetBalance(String assetId) {
        for (AssetBalance ab : assetBalances.balances) {
            if (ab.isAssetId(assetId)) return ab;
        }
        return wavesAsset;
    }

    public List<Transaction> getAssetTransactions(AssetBalance ab) {
        List<Transaction> txs = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (tx.isForAsset(ab.assetId)) txs.add(tx);
        }
        return txs;
    }

    public List<Transaction> getPendingAssetTransactions(AssetBalance ab) {
        List<Transaction> txs = new ArrayList<>();
        for (Transaction tx : pendingTransactions) {
            if (tx.isForAsset(ab.assetId)) txs.add(tx);
        }
        return txs;
    }

    public void addPendingTransaction(Transaction tx) {
        pendingTransactions.add(0, tx);
    }

    public void addPendingAsset(AssetBalance ab) {
        pendingAssets.add(0, ab);
    }

    public long getWavesBalance() {
        return getAssetBalance(null).balance;
    }
}
