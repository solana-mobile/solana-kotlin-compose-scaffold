package com.example.solanakotlincomposescaffold.usecase

import android.content.SharedPreferences
import com.solana.publickey.SolanaPublicKey
import javax.inject.Inject

sealed class WalletConnection

object NotConnected : WalletConnection()

data class Connected(
    val publicKey: SolanaPublicKey,
    val accountLabel: String,
    val authToken: String
): WalletConnection()

class PersistenceUseCase @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {

    private var connection: WalletConnection = NotConnected

    fun getWalletConnection(): WalletConnection {
        return when(connection) {
            is Connected -> connection
            is NotConnected -> {
                val key = sharedPreferences.getString(PUBKEY_KEY, "")
                val accountLabel = sharedPreferences.getString(ACCOUNT_LABEL, "") ?: ""
                val token = sharedPreferences.getString(AUTH_TOKEN_KEY, "")

                val newConn = if (key.isNullOrEmpty() || token.isNullOrEmpty()) {
                    NotConnected
                } else {
                    Connected(SolanaPublicKey.from(key), accountLabel, token)
                }

                return newConn
            }
        }
    }

    fun persistConnection(pubKey: SolanaPublicKey, accountLabel: String, token: String) {
        sharedPreferences.edit().apply {
            putString(PUBKEY_KEY, pubKey.base58())
            putString(ACCOUNT_LABEL, accountLabel)
            putString(AUTH_TOKEN_KEY, token)
        }.apply()

        connection = Connected(pubKey, accountLabel, token)
    }

    fun clearConnection() {
        sharedPreferences.edit().apply {
            putString(PUBKEY_KEY, "")
            putString(ACCOUNT_LABEL, "")
            putString(AUTH_TOKEN_KEY, "")
        }.apply()

        connection = NotConnected
    }

    companion object {
        const val PUBKEY_KEY = "stored_pubkey"
        const val ACCOUNT_LABEL = "stored_account_label"
        const val AUTH_TOKEN_KEY = "stored_auth_token"
    }
}