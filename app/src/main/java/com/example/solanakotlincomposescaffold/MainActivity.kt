package com.example.solanakotlincomposescaffold

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.solanakotlincomposescaffold.ui.theme.SolanaKotlinComposeScaffoldTheme
import com.example.solanakotlincomposescaffold.viewmodel.MainViewModel
import com.solana.mobilewalletadapter.clientlib.ActivityResultSender
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.solana.publickey.SolanaPublicKey
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.funkatronics.encoders.Base58

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sender = ActivityResultSender(this)

        setContent {
            SolanaKotlinComposeScaffoldTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column() {
                        MainScreen(sender)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun MainScreen(
    intentSender: ActivityResultSender? = null,
    viewModel: MainViewModel = hiltViewModel()
) {
    val viewState by viewModel.viewState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Text(
                text = "Solana Compose dApp Scaffold",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(all = 24.dp)
            )
        },
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onBackground,
        contentWindowInsets = WindowInsets(12, 12, 12, 12),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->

        LaunchedEffect(Unit) {
            viewModel.loadConnection()
        }

        LaunchedEffect(viewState.snackbarMessage) {
            viewState.snackbarMessage?.let { message ->
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                viewModel.clearSnackBar()
            }
        }

        Column(
            modifier = Modifier
                .padding(padding)
        ) {

            Section(
                sectionTitle = "Messages:",
            ) {
                Button(
                    onClick = {
                        if (intentSender != null && viewState.canTransact)
                            viewModel.signMessage(intentSender, "Hello Solana!")
                        else
                            viewModel.disconnect()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Sign a message")
                }
            }

            Section(
                sectionTitle = "Transactions:",
            ) {
                Button(
                    onClick = {
                        if (intentSender != null && viewState.canTransact)
                            viewModel.signTransaction(intentSender)
                        else
                            viewModel.disconnect()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Sign a Transaction (deprecated)")
                }
                Button(
                    onClick = {
                        if (intentSender != null && viewState.canTransact)
                            viewModel.publishMemo(intentSender, "Hello Solana!")
                        else
                            viewModel.disconnect()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Send a Memo Transaction")
                }

                val memoTxSignature = viewState.memoTxSignature
                if (memoTxSignature != null) {
                    ExplorerHyperlink(memoTxSignature)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (viewState.canTransact)
                AccountInfo(
                    walletName = viewState.userLabel,
                    address = viewState.userAddress,
                    balance = viewState.solBalance
                )

            Row() {
                if (viewState.canTransact)
                    Button(
                        onClick = {
                            viewModel.requestAirdrop(SolanaPublicKey(Base58.decode(viewState.userAddress)))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                            .fillMaxWidth()

                    ) {
                        Text("Request Airdrop")
                    }
                Button(
                    onClick = {
                        if (intentSender != null && !viewState.canTransact)
                            viewModel.connect(intentSender)
                        else
                            viewModel.disconnect()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .fillMaxWidth()
                ) {
                    Text(if (viewState.canTransact) "Disconnect" else "Connect")
                }
            }
        }
    }
}


@Composable
fun Section(sectionTitle: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.padding(bottom = 16.dp)) {
        Text(
            text = sectionTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        content()
    }
}

@Composable
fun AccountInfo(walletName: String, address: String, balance: Number) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .padding(bottom = 8.dp)
            .fillMaxWidth()
            .border(1.dp, Color.Black, RoundedCornerShape(16.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Connected Wallet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
            )
            // Wallet name and address
            Text(
                text = "$walletName ($address)",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Account balance
            Text(
                text = "$balance SOL", // TODO: Nicely format the displayed number (e.g: 0.089 SOL)
                style = MaterialTheme.typography.headlineLarge
            )
        }
    }
}

@Composable
fun ExplorerHyperlink(txSignature: String) {
    val context = LocalContext.current
    val url = "https://explorer.solana.com/tx/${txSignature}?cluster=devnet"
    val annotatedText = AnnotatedString.Builder("View your memo on the ").apply {
        pushStyle(
            SpanStyle(
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
                fontSize = 16.sp
            )
        )
        append("explorer.")
    }

    ClickableText(
        text = annotatedText.toAnnotatedString(),
        onClick = {
            openUrl(context, url)
        }
    )
}

fun openUrl(context: Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}

