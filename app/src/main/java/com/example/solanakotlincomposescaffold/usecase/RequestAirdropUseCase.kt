package com.example.solanakotlincomposescaffold.usecase

import android.net.Uri
import android.util.Log
import com.example.solanakotlincomposescaffold.networking.KtorHttpDriver
import com.solana.networking.Rpc20Driver
import com.solana.publickey.SolanaPublicKey
import com.solana.rpccore.JsonRpc20Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import java.util.UUID

object RequestAirdropUseCase {
    private val TAG = RequestAirdropUseCase::class.simpleName

    suspend operator fun invoke(rpcUri: Uri, address: SolanaPublicKey, lamports: Long): String =
        withContext(Dispatchers.IO) {
            val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
            val requestId = UUID.randomUUID().toString()
            val request = createAirdropRequest(address, lamports, requestId)
            val response = rpc.makeRequest(request, String.serializer())

            response.error?.let { error ->
                throw AirdropFailedException("Airdrop failed: ${error.code}, ${error.message}")
            }

            Log.d(TAG, "requestAirdrop pubKey=${address.base58()}, signature(base58)=${response.result}")

            response.result ?: throw AirdropFailedException("Airdrop failed: Unknown Error")
        }

    private fun createAirdropRequest(address: SolanaPublicKey, lamports: Long, requestId: String = "1") =
        JsonRpc20Request(
            method = "requestAirdrop",
            params = buildJsonArray {
                add(address.base58())
                add(lamports)
            },
            requestId
        )

    class AirdropFailedException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}