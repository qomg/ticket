package com.example.ticket

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import com.example.ticket.ui.theme.MyApplicationTheme
import com.google.android.gms.instantapps.InstantApps
import com.google.android.gms.instantapps.PackageManagerCompat

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        modifier = Modifier
                            .padding(innerPadding)
                            .clickable {
//                                startTechDiscovered()
//                                return@clickable
//                                startNfcReader()
//                                return@clickable
//                                startNdefDiscovered()
//                                return@clickable
                                showInstallPrompt()
                            },
                        text = "MainActivity",
                    )
                }
            }
        }

        //launchMarket()
    }

//    private fun startTechDiscovered() {
//        val intent = NfcTagHelper.makeIntent(packageName) ?: return
//        startActivity(intent)
//    }
//
//    private fun startNdefDiscovered() {
//        startActivity(
//            Intent(NfcAdapter.ACTION_TECH_DISCOVERED)
//                .setType("application/vnd.com.example.ticket")
//                //.setData("vnd.android.nfc://ext/abc:d".toUri())
//        )
//    }
//
//    private fun startNfcReader() {
//        startActivity(
//            Intent(this, NfcReaderActivity::class.java)
//        )
//    }

    private fun showInstallPrompt() {
        if (InstantApps.getPackageManagerCompat(this).isInstantApp) {
            val postInstall = Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setPackage(packageName)
            // The request code is passed to startActivityForResult().
            InstantApps.showInstallPrompt(this, postInstall, 1, /* referrer= */ null)
        }
    }

    private fun launchMarket() {
        val intent = Intent(Intent.ACTION_VIEW)
            .setData("market://launch?id=com.hotpads.mobile".toUri())
//            .setData("https://play.google.com/store/apps/details?id=com.hotpads.mobile&launch=true".toUri())
            //.setPackage("com.android.vending")
        startActivity(intent)
    }
}