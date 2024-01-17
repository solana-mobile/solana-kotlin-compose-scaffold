package com.example.solanakotlincomposescaffold.usecase

import android.net.Uri
import android.util.Log
import com.example.solanakotlincomposescaffold.networking.KtorHttpDriver
import com.funkatronics.encoders.Base58
import com.solana.networking.Rpc20Driver
import com.solana.rpccore.JsonRpc20Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray

object SendTransactionsUseCase {
    private val TAG = SendTransactionsUseCase::class.simpleName

    suspend operator fun invoke(rpcUri: Uri, transactions: List<ByteArray>) {
        withContext(Dispatchers.IO) {
            val signatures = MutableList<String?>(transactions.size) { null }
            transactions.forEachIndexed { i, transaction ->
                val rpc = Rpc20Driver(rpcUri.toString(), KtorHttpDriver())
                val request = createSendTransactionRequest(transaction, i.toString())
                val response = rpc.makeRequest(request, String.serializer())
                signatures[i] = if (response.error == null) response.result else {
                    Log.e(TAG, "Failed sending transaction: ${response.error!!.code}")
                    null
                }

                if (signatures.contains(null))
                    throw InvalidTransactionsException(signatures.map { it != null }.toBooleanArray())
            }
        }
    }

    private fun createSendTransactionRequest(transaction: ByteArray, requestId: String = "1") =
        JsonRpc20Request(
            method = "sendTransaction",
            params = buildJsonArray {
                add(Base58.encodeToString(transaction))
            },
            requestId
        )

    class InvalidTransactionsException(val valid: BooleanArray, message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)
}