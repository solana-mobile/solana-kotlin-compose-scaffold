package com.example.solanakotlincomposescaffold

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.solanakotlincomposescaffold.ui.theme.SolanaKotlinComposeScaffoldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SolanaKotlinComposeScaffoldTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column() {
                        // Header
                        Text(
                            text = "Solana Compose dApp Scaffold",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(all = 24.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreen() {
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        Section(
            sectionTitle = "Messages:",
        ) {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sign a message")
            }
        }

        Section(
            sectionTitle = "Transactions:",
        ) {
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Sign a Transaction")
            }
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Send a Transaction")
            }
        }
        


        Spacer(modifier = Modifier.weight(1f))  // This will push the remaining content to the center

        AccountInfo(walletName = "Phantom-wallet", address = "abcdefg123", balance = 102423)
        Row() {
            Button(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp)
                    .fillMaxWidth()

            ) {
                Text("Request Airdrop")
            }
            Button(onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                        .fillMaxWidth()) {
                Text("Disconnect")
            } // TODO: Flip to 'Connect' when disconnected/no authToken
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
