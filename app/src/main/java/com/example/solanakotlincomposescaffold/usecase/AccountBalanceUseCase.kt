package com.example.solanakotlincomposescaffold.usecase

import android.net.Uri
import android.util.Log
import com.example.solanakotlincomposescaffold.networking.KtorHttpDriver
import com.solana.networking.Rpc20Driver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonArray
import com.solana.publickey.SolanaPublicKey
import com.solana.rpccore.JsonRpc20Request
import kotlinx.serialization.json.add

object AccountBalanceUseCase {
    private val TAG = AccountBalanceUseCase::class.simpleName
    suspend operator fun invoke(rpcUri: Uri, address: SolanaPublicKey): Long =
        withContext(Dispatchers.IO) {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val requestId = UUID.randomUUID().toString()
            val request = createBalanceRequest(address, requestId)
            val response = rpc.makeRequest(request, BalanceResponse.serializer())

            response.error?.let { error ->
                throw InvalidAccountException("Could not fetch balance for account [${address.base58()}]: ${error.code}, ${error.message}")
            }

            Log.d(TAG, "getBalance pubKey=${address.base58()}, balance=${response.result}")
            return@withContext response.result!!.value
        }

    private fun createBalanceRequest(address: SolanaPublicKey, requestId: String = "1") =
        JsonRpc20Request(
            method = "getBalance",
            params = buildJsonArray {
                add(address.base58())
            },
            requestId
        )

    @Serializable
    data class BalanceResponse(val value: Long)
    class InvalidAccountException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}