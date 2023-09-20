package com.example.solanakotlincomposescaffold.usecase

import android.net.Uri
import android.util.Log
import com.example.solanakotlincomposescaffold.networking.KtorHttpDriver
import com.funkatronics.encoders.Base58
import com.funkatronics.networking.Rpc20Driver
import com.funkatronics.rpccore.JsonRpc20Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import java.util.UUID

object AccountBalanceUseCase {
    private val TAG = AccountBalanceUseCase::class.simpleName
    suspend operator fun invoke(rpcUri: Uri, address: ByteArray): Long =
        withContext(Dispatchers.IO) {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val requestId = UUID.randomUUID().toString()
            val request = createBalanceRequest(address, requestId)
            val response = rpc.makeRequest(request, BalanceResponse.serializer())

            response.error?.let { error ->
                throw InvalidAccountException("Could not fetch balance for account [${Base58.encodeToString(address)}]: ${error.code}, ${error.message}")
            }

            Log.d(TAG, "getBalance pubKey=${Base58.encodeToString(address)}, balance=${response.result}")
            return@withContext response.result!!.value
        }

    private fun createBalanceRequest(address: ByteArray, requestId: String = "1") =
        JsonRpc20Request(
            method = "getBalance",
            params = buildJsonArray {
                add(Base58.encodeToString(address))
            },
            requestId
        )

    @Serializable
    data class BalanceResponse(val value: Long)
    class InvalidAccountException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}