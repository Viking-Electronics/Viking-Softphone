package com.vikingelectronics.softphone.extensions

import org.linphone.core.Account
import org.linphone.core.AccountParams
import org.linphone.core.Core

fun Core.createAccountWithParams(paramsInitializer: AccountParams.() -> Unit): Account {
    val params = createAccountParams().apply(paramsInitializer)
    return createAccount(params)
}