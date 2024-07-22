package com.example.ticket

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import java.nio.charset.Charset

class LoyaltyCardReader : NfcAdapter.ReaderCallback {

    override fun onTagDiscovered(tag: Tag?) {
        tag?.let {
            IsoDep.get(it)
        }?.runCatching {
            connect()
            val cmd = IsoDepHelper.BuildSelectApdu(IsoDepHelper.SAMPLE_LOYALTY_CARD_AID)
            IsoDepHelper.ByteArrayToHexString(cmd)
            val result = transceive(cmd)
            val len = result.size
            val statusWord = result.takeLast(2)
            val payload = result.take(len - 2)
            if (statusWord[0] == 0x90.toByte() && statusWord[1] == 0x00.toByte()) {
                val account = String(payload.toByteArray(), Charset.forName("UTF-8"))
                println(account)
            }
        }?.onFailure {
            it.printStackTrace()
        }
    }
}