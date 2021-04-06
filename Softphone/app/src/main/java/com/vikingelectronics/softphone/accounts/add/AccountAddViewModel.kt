package com.vikingelectronics.softphone.accounts.add

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

sealed class AccountAddAction {
    object LoginClicked: AccountAddAction()
    data class UsernameUpdated(val username: String, val showError: Boolean = false): AccountAddAction()
    data class UserIdUpdated(val userId: String): AccountAddAction()
    data class PasswordUpdated(val password: String): AccountAddAction()
    data class DomainUpdated(val domain: String): AccountAddAction()
    data class DisplayNameUpdated(val displayName: String): AccountAddAction()
    data class TransportUpdated(val transport: AccountAddState.TransportType): AccountAddAction()
}


@HiltViewModel
class AccountAddViewModel @Inject constructor(
        savedStateHandle: SavedStateHandle,
): ViewModel() {

    var username: State<String> = mutableStateOf("")

//    override val update = update<AccountAddState, AccountAddAction> { action ->
//        when(action) {
//            is AccountAddAction.LoginClicked -> copy()
//            is AccountAddAction.UsernameUpdated -> copy(username = action.username, usernameError = action.showError)
//            is AccountAddAction.UserIdUpdated -> copy(userId = action.userId)
//            is AccountAddAction.PasswordUpdated -> copy(password = action.password)
//            is AccountAddAction.DomainUpdated -> copy(domain = action.domain)
//            is AccountAddAction.DisplayNameUpdated -> copy(displayName = action.displayName)
//            is AccountAddAction.TransportUpdated -> copy(transportType = action.transport)
//        }
//    }
//    override val interceptions = interceptions<AccountAddState, AccountAddAction> {
//        on<AccountAddAction.UsernameUpdated> {
//            adapter { _, action: AccountAddAction.UsernameUpdated ->
//                AccountAddAction.UsernameUpdated(action.username, action.username.isBlank())
//            }
//        }
//        on<AccountAddAction.LoginClicked> {
//            filter { state: AccountAddState, _ ->
//                state.username.isNotBlank() && state.password.isNotBlank() && state.domain.isNotBlank()
//            }
////            stateFlow.
//        }
//    }

//    fun usernameUpdated(username: CharSequence?) = dispatch(AccountAddAction.UsernameUpdated(username.toString()))
//    fun usernameUpdated(username: String) = dispatch(AccountAddAction.UsernameUpdated(username))
//
//    fun userIdUpdated(userId: CharSequence?) = dispatch(AccountAddAction.UserIdUpdated(userId.toString()))
//
//    fun passwordUpdated(password: CharSequence?) = dispatch(AccountAddAction.PasswordUpdated(password.toString()))
//
//    fun domainUpdated(domain: CharSequence?) = dispatch(AccountAddAction.DomainUpdated(domain.toString()))
//
//    fun displayNameUpdated(displayName: CharSequence?) = dispatch(AccountAddAction.DisplayNameUpdated(displayName.toString()))
//
//    fun transportUpdated(type: AccountAddState.TransportType) = dispatch(AccountAddAction.TransportUpdated(type))
//
//    fun loginClicked() = dispatch(AccountAddAction.LoginClicked)
}