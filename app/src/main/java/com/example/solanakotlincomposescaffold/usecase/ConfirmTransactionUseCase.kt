package com.example.solanakotlincomposescaffold.usecase

import android.net.Uri
import android.util.Log
import com.example.solanakotlincomposescaffold.networking.KtorHttpDriver
import com.solana.networking.Rpc20Driver
import com.solana.rpccore.JsonRpc20Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID

object ConfirmTransactionUseCase {
    private val TAG = ConfirmTransactionUseCase::class.simpleName

    suspend operator fun invoke(rpcUri: Uri, signature: String): Boolean =
        withContext(Dispatchers.IO) {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val requestId = UUID.randomUUID().toString()
            val request = createSignatureStatusRequest(signature, true, requestId)
            var confirmed = false
            val timeoutMillis = 30000
            val startTime = System.currentTimeMillis()
            val targetConfirmations = 31

            while (!confirmed) {

                val response = rpc.makeRequest(request, SignatureStatusResponse.serializer())

                response.error?.let { error ->
                    throw InvalidTransactionSignature("Signature Status Invalid: ${error.code}, ${error.message}")
                }

                response.result?.value?.find { it?.err != null }?.let { erroredStatus ->
                    throw SignatureStatusError(erroredStatus.err.toString())
                }

                Log.d(TAG, "getSignatureStatuses: signature = $signature, status=${response.result?.value}")

                val confirmations = response.result?.value?.get(0)?.confirmations ?: 0
                confirmed = response.result?.value?.get(0)?.confirmationStatus == "finalized"

                val retryTime = (targetConfirmations - confirmations)*300L

                if (!confirmed) delay(retryTime)
                if (System.currentTimeMillis() - startTime > timeoutMillis) break
            }

            confirmed
        }

    private fun createSignatureStatusRequest(signature: String, searchHistory: Boolean = true, requestId: String = "1") =
        JsonRpc20Request(
            method = "getSignatureStatuses",
            params = buildJsonArray {
                add(buildJsonArray {
                    add(signature)
                })
                add(buildJsonObject {
                    put("searchTransactionHistory", searchHistory)
                })
            },
            requestId
        )

    @Serializable
    class SignatureStatusResponse(val value: List<SignatureStatus?>)

    @Serializable
    data class SignatureStatus(
        val slot: Long,
        val confirmations: Int?,
        val err: JsonElement?,
        val confirmationStatus: String?
    )

    sealed class SignatureStatusException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
    class InvalidTransactionSignature(message: String? = null, cause: Throwable? = null) : SignatureStatusException(message, cause)
    class SignatureStatusError(message: String? = null, cause: Throwable? = null) : SignatureStatusException(message, cause)
}