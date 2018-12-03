package com.wavesplatform.wallet.v2.util

import android.content.Context
import com.evgenii.jsevaluator.JsEvaluator
import com.evgenii.jsevaluator.interfaces.JsCallback
import java.io.IOException
import java.util.*

open class JSEvaluator(private var context: Context) {

    private var jsEvaluator: JsEvaluator = JsEvaluator(this.context)
    private var jsCode: String = ""

    /*function strengthenPassword(password: string, rounds: number = 5000): string {
        while (rounds--) password = converters.byteArrayToHexString(sha256(password));
        return password;
    }*/

    fun decryptAndResult(password: String, encrypted: String, onResult: JsCallback) {
        jsCode += "var jsEvaluatorResult = ''; "
        jsCode += loadJs("crypto-js.js")
        jsCode += "; "
        jsCode += ("var decrypted = CryptoJS.AES.decrypt($encrypted, '$password');"
                + "jsEvaluatorResult += ' ' + decrypted.toString(CryptoJS.enc.Utf8); ")
        jsCode += "jsEvaluatorResult;"
        jsEvaluator.evaluate(jsCode, onResult)
    }

    fun encryptAndResult(password: String, textToDecrypt: String, onResult: JsCallback) {
        jsCode += "var jsEvaluatorResult = ''; "
        jsCode += loadJs("crypto-js.js")
        jsCode += "; "
        jsCode += ("var encrypted = CryptoJS.AES.encrypt($textToDecrypt, $password);"
                + "jsEvaluatorResult += ' ' + encrypted.toString(CryptoJS.enc.Utf8); ")
        jsCode += "jsEvaluatorResult;"
        jsEvaluator.evaluate(jsCode, onResult)
    }

    @Throws(IOException::class)
    private fun readFile(fileName: String): String {
        val am = context.assets
        val inputStream = am.open(fileName)

        val scanner = Scanner(inputStream, "UTF-8")
        return scanner.useDelimiter("\\A").next()
    }

    private fun loadJs(fileName: String): String {
        try {
            return readFile(fileName)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}