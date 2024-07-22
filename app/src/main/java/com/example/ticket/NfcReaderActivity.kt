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
import com.example.ticket.ui.theme.MyApplicationTheme
import com.google.android.gms.instantapps.InstantApps

class NfcReaderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Text(
                        modifier = Modifier.padding(innerPadding),
                        text = "NfcReaderActivity",
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        disableReaderMode()
    }

    private fun enableReaderMode() {
        NfcAdapter.getDefaultAdapter(this)?.enableReaderMode(
            this,
            LoyaltyCardReader(),
            /*NfcAdapter.FLAG_READER_NFC_A or */NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    private fun disableReaderMode() {
        NfcAdapter.getDefaultAdapter(this)?.disableReaderMode(this)
    }

}