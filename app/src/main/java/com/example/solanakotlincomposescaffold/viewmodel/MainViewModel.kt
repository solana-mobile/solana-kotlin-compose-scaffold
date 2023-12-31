package com.example.solanakotlincomposescaffold.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solanakotlincomposescaffold.usecase.AccountBalanceUseCase
import com.example.solanakotlincomposescaffold.usecase.ConfirmTransactionUseCase
import com.example.solanakotlincomposescaffold.usecase.Connected
import com.example.solanakotlincomposescaffold.usecase.PersistenceUseCase
import com.example.solanakotlincomposescaffold.usecase.RequestAirdropUseCase
import com.funkatronics.publickey.SolanaPublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainViewState(
    val isLoading: Boolean = false,
    val canTransact: Boolean = false,
    val solBalance: Double = 0.0,
    val userAddress: String = "",
    val userLabel: String = "",
    val walletFound: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase
): ViewModel() {

    private val rpcUri = "https://api.testnet.solana.com".toUri()
    private fun MainViewState.updateViewState() {
        _state.update { this }
    }

    private val _state = MutableStateFlow(MainViewState())

    val viewState: StateFlow<MainViewState>
        get() = _state

    fun loadConnection() {
        val persistedConn = persistenceUseCase.getWalletConnection()

        if (persistedConn is Connected) {
            _state.value.copy(
                isLoading = true,
                canTransact = true,
                userAddress = persistedConn.publicKey.base58(),
                userLabel = persistedConn.accountLabel,
            ).updateViewState()

            getBalance(persistedConn.publicKey)

            _state.value.copy(
                isLoading = false
            ).updateViewState()

            walletAdapter.authToken = persistedConn.authToken
        }
    }

    fun connect(sender: ActivityResultSender) {
        viewModelScope.launch {
            when (val result = walletAdapter.connect(sender)) {
                is TransactionResult.Success -> {
                    val currentConn = Connected(
                        SolanaPublicKey(result.authResult.publicKey),
                        result.authResult.accountLabel ?: "",
                        result.authResult.authToken
                    )

                    persistenceUseCase.persistConnection(
                        currentConn.publicKey,
                        currentConn.accountLabel,
                        currentConn.authToken
                    )

                    _state.value.copy(
                        isLoading = true,
                        userAddress = currentConn.publicKey.base58(),
                        userLabel = currentConn.accountLabel
                    ).updateViewState()

                    getBalance(currentConn.publicKey)

                    _state.value.copy(
                        isLoading = false,
                        canTransact = true
                    ).updateViewState()
                }

                is TransactionResult.NoWalletFound -> {
                    _state.value.copy(
                        walletFound = false
                    ).updateViewState()

                }

                is TransactionResult.Failure -> {
                    _state.value.copy(
                        isLoading = false,
                        canTransact = false,
                        userAddress = "",
                        userLabel = "",
                    ).updateViewState()
                }
            }
        }
    }

    fun getBalance(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result =
                    AccountBalanceUseCase(rpcUri, account)

                _state.value.copy(
                    solBalance = result/1000000000.0
                ).updateViewState()
            } catch (e: AccountBalanceUseCase.InvalidAccountException) {
                // TODO: communicate error to UI
            }
        }
    }

    fun requestAirdrop(account: SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val signature = RequestAirdropUseCase(rpcUri, account, 10000)

                ConfirmTransactionUseCase(rpcUri, signature)

                getBalance(account)
            } catch (e: RequestAirdropUseCase.AirdropFailedException) {
                // TODO: communicate error to UI
            } catch (e: ConfirmTransactionUseCase.SignatureStatusException) {
                // TODO: communicate error to UI
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            val conn = persistenceUseCase.getWalletConnection()
            if (conn is Connected) {
                persistenceUseCase.clearConnection()

                MainViewState().updateViewState()
            }
        }
    }
}