package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes

import java.util.Locale

import io.github.anderscheow.validator.rules.BaseRule

class EqualRule : BaseRule {

    private var keyword: String? = null

    constructor(keyword: String?) : super(String.format(Locale.getDefault(), "Value does not equal to '%s'", keyword)) {
        this.keyword = keyword
    }

    constructor(keyword: String?, @StringRes errorRes: Int) : super(errorRes) {
        this.keyword = keyword
    }

    constructor(keyword: String?, errorMessage: String) : super(errorMessage) {
        this.keyword = keyword
    }

    override fun validate(value: Any): Boolean {
        if (value is String) {
            return value == keyword
        }

        throw ClassCastException("Required String value")
    }
}
