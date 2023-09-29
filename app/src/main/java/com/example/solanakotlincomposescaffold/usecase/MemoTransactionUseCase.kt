package com.example.solanakotlincomposescaffold.usecase

import android.net.Uri
import com.funkatronics.publickey.SolanaPublicKey
import com.funkatronics.transaction.AccountMeta
import com.funkatronics.transaction.Message
import com.funkatronics.transaction.TransactionInstruction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import com.funkatronics.publickey.SolanaPublicKey as PublicKey

object MemoTransactionUseCase {
    private val TAG = AccountBalanceUseCase::class.simpleName
    private val memoProgramId = "MemoSq4gqABAXKb96qnH8TysNcWxMyWCqXgDLGmfcHr"

    suspend operator fun invoke(rpcUri: Uri, address: PublicKey, message: String): Message =
        withContext(Dispatchers.IO) {
            // Solana Memo Program
            val memoProgramId = SolanaPublicKey.from(memoProgramId)
            val memoInstruction = TransactionInstruction(
                memoProgramId,
                listOf(AccountMeta(address, true, true)),
                message.encodeToByteArray()
            )

            // Build Message
            val blockhash = RecentBlockhashUseCase(rpcUri)
            Message.Builder()
                .addInstruction(memoInstruction)
                .setRecentBlockhash(blockhash)
                .build()
        }
}