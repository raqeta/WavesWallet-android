package com.wavesplatform.wallet.v1.data;

public class Events {
    public static class NeedUpdateDataAfterPlaceOrder {

    }

    public static class ChangeTimeFrame{
        private String timeFrame;

        public ChangeTimeFrame(String timeFrame) {
            this.timeFrame = timeFrame;
        }

        public String getTimeFrame() {
            return timeFrame;
        }
    }
}
