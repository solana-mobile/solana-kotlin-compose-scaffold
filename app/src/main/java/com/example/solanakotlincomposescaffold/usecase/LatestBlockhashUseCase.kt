package com.example.solanakotlincomposescaffold.usecase

import android.net.Uri
import android.util.Log
import com.example.solanakotlincomposescaffold.networking.KtorHttpDriver
import com.solana.networking.Rpc20Driver
import com.solana.rpccore.JsonRpc20Request
import com.solana.transaction.Blockhash
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.put
import java.util.UUID


object RecentBlockhashUseCase {
    private val TAG = RequestAirdropUseCase::class.simpleName

    suspend operator fun invoke(rpcUri: Uri, commitment: String = "confirmed"): Blockhash =
        withContext(Dispatchers.IO) {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val requestId = UUID.randomUUID().toString()
            val request = createBlockhashRequest(commitment, requestId)
            val response = rpc.makeRequest(request, BlockhashResponse.serializer())

            response.error?.let { error ->
                throw BlockhashException("Could not fetch latest blockhash: ${error.code}, ${error.message}")
            }

            Log.d(TAG, "getLatestBlockhash blockhash=${response.result?.value?.blockhash}")

            Blockhash.from(response.result?.value?.blockhash
                ?: throw BlockhashException("Could not fetch latest blockhash: UnknownError"))
        }

    private fun createBlockhashRequest(commitment: String = "confirmed", requestId: String = "1") =
        JsonRpc20Request(
            method = "getLatestBlockhash",
            params = buildJsonArray {
                addJsonObject {
                    put("commitment", commitment)
                }
            },
            requestId
        )


    @Serializable
    class BlockhashResponse(val value: BlockhashInfo)

    @Serializable
    class BlockhashInfo(
        val blockhash: String,
        val lastValidBlockHeight: Long
    )

    class BlockhashException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}