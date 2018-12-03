package com.wavesplatform.wallet.v1.data.connectivity;

import android.content.Context;

public enum ConnectivityManager {

    INSTANCE;

    ConnectivityManager() {
        // No-op
    }

    public static ConnectivityManager getInstance() {
        return INSTANCE;
    }

    /**
     * Listens for network connection events using whatever is best practice for the current API level,
     */
    public void registerNetworkListener(Context context) {
        new ConnectionStateMonitor(context).enable();
    }
}
