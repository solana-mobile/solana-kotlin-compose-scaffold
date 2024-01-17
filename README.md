# SolanaKotlinComposeScaffold

A boilerplate example app for Solana Mobile dApps built using Jetpack Compose.
It provides an interface to connect to locally installed wallet apps (that are MWA-compatible), view your account balance on devnet, and request an airdrop of SOL.

## Featured Libarires

- [Mobile Wallet Adapter (`clientlib-ktx`)](https://github.com/solana-mobile/mobile-wallet-adapter/tree/main/js/packages/mobile-wallet-adapter-protocol) for connecting to wallets and signing transactions/messages
- [web3-solana](https://github.com/solana-mobile/web3-core) for Solana primitive types (`SolanaPublicKey`) and constructing messages/transactions.
- [rpc-core](https://github.com/solana-mobile/rpc-core) for constructing and sending [Solana RPC requests](https://docs.solana.com/api/http).

<table>
  <tr>
    <td align="center">
      <img src="/screenshots/screenshot1.png" alt="Scaffold dApp Screenshot 1" width=300 />
    </td>
    <td align="center">
      <img src="/screenshots/screenshot2.png" alt="Scaffold dApp Screenshot 2" width=300 />
    </td>
    <td align="center">
      <img src="/screenshots/screenshot3.png" alt="Scaffold dApp Screenshot 3" width=300 />
    </td>
  </tr>
</table>

## Prerequisites

You'll need to first setup your environment for Android development. Follow the [Prerequisite Setup Guide](https://docs.solanamobile.com/getting-started/development-setup).

Follow the guide to make sure you:

- setup your Android and React Native development environment.
- have an Android device or emulator.
- install an MWA compliant wallet app on your device/emulator.

## Usage

1. Initialize project template

```bash
git clone https://github.com/solana-mobile/solana-kotlin-compose-scaffold.git
```

2. Open the project on Android Studio > Open > "SolanaKotlinComposeScaffold/app/build.gradle.kts"

3. Start your emulator/device and build the app

## Troubleshooting

- `Compatible wallet not found.`
  - Make sure you install a compatible MWA wallet on your device, like Phantom, Solflare, Ultimate, or `fakewallet`. Follow
    the [setup guide](https://docs.solanamobile.com/getting-started/development-setup).
