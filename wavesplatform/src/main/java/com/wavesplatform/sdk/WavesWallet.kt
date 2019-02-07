package com.wavesplatform.sdk

import com.wavesplatform.sdk.crypto.AESUtil
import com.wavesplatform.sdk.crypto.Base58
import com.wavesplatform.sdk.crypto.PrivateKeyAccount
import com.wavesplatform.sdk.utils.AddressUtil

class WavesWallet(val seed: ByteArray) {
    private val account: PrivateKeyAccount = PrivateKeyAccount(seed)
    val address: String

    val publicKeyStr: String
        get() = account.publicKeyStr

    val privateKey: ByteArray
        get() = account.privateKey

    val privateKeyStr: String
        get() = account.privateKeyStr

    val seedStr: String
        get() = String(seed, Charsets.UTF_8)

    init {
        address = AddressUtil.addressFromPublicKey(account.publicKey)
    }

    @Throws(Exception::class)
    constructor(walletData: String, password: String) : this(Base58.decode(
            AESUtil.decrypt(walletData, password, DEFAULT_PBKDF2_ITERATIONS_V2))) {
    }

    @Throws(Exception::class)
    fun getEncryptedData(password: String): String {
        return AESUtil.encrypt(Base58.encode(seed), password, DEFAULT_PBKDF2_ITERATIONS_V2)
    }

    companion object {
        const val DEFAULT_PBKDF2_ITERATIONS_V2 = 5000
    }
}