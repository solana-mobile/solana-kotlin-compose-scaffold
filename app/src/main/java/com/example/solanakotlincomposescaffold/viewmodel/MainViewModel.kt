package com.example.solanakotlincomposescaffold.viewmodel

import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.solanakotlincomposescaffold.BuildConfig
import com.example.solanakotlincomposescaffold.usecase.AccountBalanceUseCase
import com.example.solanakotlincomposescaffold.usecase.ConfirmTransactionUseCase
import com.example.solanakotlincomposescaffold.usecase.Connected
import com.example.solanakotlincomposescaffold.usecase.MemoTransactionUseCase
import com.example.solanakotlincomposescaffold.usecase.PersistenceUseCase
import com.example.solanakotlincomposescaffold.usecase.RequestAirdropUseCase
import com.funkatronics.encoders.Base58
import com.solana.publickey.SolanaPublicKey
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import com.solana.mobilewalletadapter.clientlib.MobileWalletAdapter
import com.solana.mobilewalletadapter.clientlib.TransactionResult
import com.solana.mobilewalletadapter.clientlib.successPayload
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
    val walletFound: Boolean = true,
    val memoTxSignature: String? = null,
    val snackbarMessage: String? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val walletAdapter: MobileWalletAdapter,
    private val persistenceUseCase: PersistenceUseCase
): ViewModel() {

    private val rpcUri = BuildConfig.RPC_URI.toUri()

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
                isLoading = false,
                // TODO: Move all Snackbar message strings into resources
                snackbarMessage = "✅ | Successfully auto-connected to: \n" + persistedConn.publicKey.base58() + "."
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
                        canTransact = true,
                        snackbarMessage = "✅ | Successfully connected to: \n" + currentConn.publicKey.base58() + "."
                    ).updateViewState()
                }

                is TransactionResult.NoWalletFound -> {
                    _state.value.copy(
                        walletFound = false,
                        snackbarMessage = "❌ | No wallet found."
                    ).updateViewState()

                }

                is TransactionResult.Failure -> {
                    _state.value.copy(
                        isLoading = false,
                        canTransact = false,
                        userAddress = "",
                        userLabel = "",
                        snackbarMessage = "❌ | Failed connecting to wallet: " + result.e.message
                    ).updateViewState()
                }
            }
        }
    }

    fun signMessage(sender: ActivityResultSender, message: String) {
        viewModelScope.launch {
            val result = walletAdapter.transact(sender) {
                signMessagesDetached(arrayOf(message.toByteArray()), arrayOf((it.accounts.first().publicKey)))
            }

            _state.value = when (result) {
                is TransactionResult.Success -> {
                    val signatureBytes = result.successPayload?.messages?.first()?.signatures?.first()
                    _state.value.copy(
                        snackbarMessage = signatureBytes?.let {
                            "✅ | Message signed: ${Base58.encodeToString(it)}"
                        } ?: "❌ | Incorrect payload returned"
                    )
                }
                is TransactionResult.NoWalletFound -> {
                    _state.value.copy(snackbarMessage = "❌ | No wallet found")
                }
                is TransactionResult.Failure -> {
                    _state.value.copy(snackbarMessage = "❌ | Message signing failed: ${result.e.message}")
                }
            }.also { it.updateViewState() }
        }
    }

    fun signTransaction(sender: ActivityResultSender ) {
        viewModelScope.launch {
            val result = walletAdapter.transact(sender) { authResult ->
                val account = SolanaPublicKey(authResult.accounts.first().publicKey)
                val memoTx = MemoTransactionUseCase(rpcUri, account, "Hello Solana!");
                signTransactions(arrayOf(
                    memoTx.serialize(),
                ));
            }

            _state.value = when (result) {
                is TransactionResult.Success -> {
                    val signedTxBytes = result.successPayload?.signedPayloads?.first()
                    signedTxBytes?.let {
                        println("Memo publish signature: " + Base58.encodeToString(signedTxBytes))
                    }
                    _state.value.copy(
                        snackbarMessage = (signedTxBytes?.let {
                            "✅ | Transaction signed: ${Base58.encodeToString(it)}"
                        } ?: "❌ | Incorrect payload returned"),
                    )
                }
                is TransactionResult.NoWalletFound -> {
                    _state.value.copy(snackbarMessage = "❌ | No wallet found")
                }
                is TransactionResult.Failure -> {
                    _state.value.copy(snackbarMessage = "❌ | Transaction failed to submit: ${result.e.message}")
                }
            }.also { it.updateViewState() }
        }
    }

    fun publishMemo(sender: ActivityResultSender, memoText: String) {
        viewModelScope.launch {
            val result = walletAdapter.transact(sender) { authResult ->
                val account = SolanaPublicKey(authResult.accounts.first().publicKey)
                val memoTx = MemoTransactionUseCase(rpcUri, account, memoText);
                signAndSendTransactions(arrayOf(memoTx.serialize()));
            }

            _state.value = when (result) {
                is TransactionResult.Success -> {
                    val signatureBytes = result.successPayload?.signatures?.first()
                    signatureBytes?.let {
                        println("Memo publish signature: " + Base58.encodeToString(signatureBytes))
                        _state.value.copy(
                            snackbarMessage = "✅ | Transaction submitted: ${Base58.encodeToString(it)}",
                            memoTxSignature = Base58.encodeToString(it)
                        )
                    } ?: _state.value.copy(
                        snackbarMessage = "❌ | Incorrect payload returned"
                    )
                }
                is TransactionResult.NoWalletFound -> {
                    _state.value.copy(snackbarMessage = "❌ | No wallet found")
                }
                is TransactionResult.Failure -> {
                    _state.value.copy(snackbarMessage = "❌ | Transaction failed to submit: ${result.e.message}")
                }
            }.also { it.updateViewState() }
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
            } catch (e: Exception) {
                _state.value.copy(
                    snackbarMessage = "❌ | Failed fetching account balance."
                ).updateViewState()
            }
        }
    }

    fun requestAirdrop(account : SolanaPublicKey) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val signature = RequestAirdropUseCase(rpcUri, account, 10000)

                if (ConfirmTransactionUseCase(rpcUri, signature)) {
                    _state.value.copy(
                        snackbarMessage = "✅ | Airdrop request succeeded!"
                    ).updateViewState()
                }

                getBalance(account)
            } catch (e: RequestAirdropUseCase.AirdropFailedException) {
                _state.value.copy(
                    snackbarMessage = "❌ | Airdrop request failed: " + e.message
                ).updateViewState()
            } catch (e: ConfirmTransactionUseCase.SignatureStatusException) {
                _state.value.copy(
                    snackbarMessage = "❌ | Signature status exception: " + e.message
                ).updateViewState()
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            val conn = persistenceUseCase.getWalletConnection()
            if (conn is Connected) {
                persistenceUseCase.clearConnection()

                MainViewState().copy(
                    snackbarMessage = "✅ | Disconnected from wallet."
                ).updateViewState()
            }
        }
    }

    fun clearSnackBar() {
        _state.value.copy(
            snackbarMessage = null
        ).updateViewState()
    }
}