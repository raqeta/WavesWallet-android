package com.wavesplatform.wallet.v1.payload;

import com.google.gson.Gson;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PayloadJsonTest {

    @Test
    public void assetBalance() {
        String abJson = "{\"assetId\":\"8Nu3gdirpraz8ghmDHscTnoAbmCTLPxLhMeVzG4UxSQY\"," +
                "\"balance\":100000000,\"reissuable\":true,\"quantity\":100000000," +
                "\"issueTransaction\":{\"type\":3,\"id\":\"8Nu3gdirpraz8ghmDHscTnoAbmCTLPxLhMeVzG4UxSQY\"," +
                "\"senderPublicKey\":\"3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v\"," +
                "\"senderPublicKey\":\"EtujXZmPthSG8YUwvF88seDcw4WH6ZPN3tRJZ6w1mev1\"," +
                "\"fee\":100000000,\"timestamp\":1485941350077," +
                "\"signature\":\"5foqUXywB3r2uDMqVCEzdf8tuwoRmRsjuWcENDBP2jCfqdoESuyA7ptY1HNRC7arLhW31v6ij87q1KDBrktCEX7b\"}}";

        Gson gson = new Gson();
        com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance ab = gson
                .fromJson(abJson, com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance.class);
        assertEquals(100000000L, ab.getBalance() == null ? 0 : ab.getBalance());
    }

    @Test
    public void assetBalances() {
        String abJson = "{\"address\":\"3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v\",\"balances\":[" +
                "{\"assetId\":\"8Nu3gdirpraz8ghmDHscTnoAbmCTLPxLhMeVzG4UxSQY\",\"balance\":100000000,\"reissuable\":true,\"quantity\":100000000," +
                "  \"issueTransaction\":{\"type\":3,\"id\":\"8Nu3gdirpraz8ghmDHscTnoAbmCTLPxLhMeVzG4UxSQY\",\"senderPublicKey\":\"3N5sUvKLnEUBwk7WFCSjJs8VoiLiuqTs29v\",\"senderPublicKey\":\"EtujXZmPthSG8YUwvF88seDcw4WH6ZPN3tRJZ6w1mev1\",\"fee\":100000000,\"timestamp\":1485941350077,\"signature\":\"5foqUXywB3r2uDMqVCEzdf8tuwoRmRsjuWcENDBP2jCfqdoESuyA7ptY1HNRC7arLhW31v6ij87q1KDBrktCEX7b\",\"assetId\":\"8Nu3gdirpraz8ghmDHscTnoAbmCTLPxLhMeVzG4UxSQY\",\"name\":\"DEXt\",\"description\":\"for DEX testing\",\"quantity\":100000000,\"decimals\":2,\"reissuable\":true}}," +
                "{\"assetId\":\"AnAU5Kp8Dev7GZHQ5nxH4FPB12qdMTYHLYMBGCNeq4pE\",\"balance\":10000000,\"reissuable\":true,\"quantity\":100000000," +
                "  \"issueTransaction\":{\"type\":3,\"id\":\"AnAU5Kp8Dev7GZHQ5nxH4FPB12qdMTYHLYMBGCNeq4pE\",\"senderPublicKey\":\"3N5GRqzDBhjVXnCn44baHcz2GoZy5qLxtTh\",\"senderPublicKey\":\"FM5ojNqW7e9cZ9zhPYGkpSP1Pcd8Z3e3MNKYVS5pGJ8Z\",\"fee\":100000000,\"timestamp\":1485946444602,\"signature\":\"42dma4RzojZAtSwMhZJRyHtCfX9ojj8vnvHVkxi4221MJoCLpGARcyHfwzmfCEtKW1RDssaW7w7DMNHuibjDHQ8r\",\"assetId\":\"AnAU5Kp8Dev7GZHQ5nxH4FPB12qdMTYHLYMBGCNeq4pE\",\"name\":\"DEXsuper\",\"description\":\"string\",\"quantity\":100000000,\"decimals\":2,\"reissuable\":true}}]}";

        Gson gson = new Gson();
        com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalances abs = gson
                .fromJson(abJson, com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalances.class);
        assertEquals(2, abs.getBalances().size());
    }

}