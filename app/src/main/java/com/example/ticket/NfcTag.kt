package com.example.ticket

import android.content.Context
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.NfcA
import android.widget.Toast
import java.nio.charset.Charset

object NfcTag {

    fun writeNfc(context: Context, tag: Tag) {
//        runCatching {
//            //TAG: Tech [android.nfc.tech.NfcA, android.nfc.tech.MifareClassic, android.nfc.tech.NdefFormatable]
//            val mifareClassic = MifareClassic.get(tag)
//            if (mifareClassic != null) {
//                mifareClassic.connect()
//                mifareClassic.readBlock(0)
////                val auth = mifareClassic.authenticateSectorWithKeyA(0, "123456".toByteArray())
////                println(auth)
////                val bytes = "https://play.google.com/store/apps/details?id=com.hotpads.mobile&launch=true".toByteArray()
////                for (i in 0..bytes.size step 16) {
////                    val block = bytes.slice(i..(i+16)).toByteArray()
////                    if (block.size == 16) {
////                        mifareClassic.writeBlock(i % 16, block)
////                    } else {
////                        val newBlock = ByteArray(16) { index ->
////                            if (index < block.size) block[index] else 0.toByte()
////                        }
////                        mifareClassic.writeBlock(i % 16, newBlock)
////                    }
////                }
////                mifareClassic.writeBlock(1, "https://play.google.com/store/apps/details?id=com.example.ticket&launch=true".toByteArray())
//            }
//        }.onFailure {
//            it.printStackTrace()
//        }
//        return
        val msg = NdefMessage(
            NdefRecord.createUri("https://play.google.com/store/apps/details?id=com.hotpads.mobile&launch=true"),
            NdefRecord.createUri("https://play.google.com/store/apps/details?id=com.example.ticket&launch=true"),
            NdefRecord.createApplicationRecord(context.packageName),
            NdefRecord.createUri("https://activity.leshuazf.com/ticket-example"),
            NdefRecord.createTextRecord("en", "hello world")
        )
        runCatching {
            val ndefFormatable = NdefFormatable.get(tag)
            if (ndefFormatable != null) {
                ndefFormatable.connect()
//                ndefFormatable.format(msg)
//                ndefFormatable.toString()
            }
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (ndef.isWritable) {
                    ndef.writeNdefMessage(msg)
                    Toast.makeText(context, "ndef message write success", Toast.LENGTH_SHORT).show()
                }
            }
        }.onFailure {
            Toast.makeText(context, "ndef message write failure: " + it.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun writeTag(tag: Tag) {
        MifareUltralight.get(tag)?.use { ultralight ->
            ultralight.connect()
            Charset.forName("US-ASCII").also { usAscii ->
                ultralight.writePage(4, "abcd".toByteArray(usAscii))
                ultralight.writePage(5, "efgh".toByteArray(usAscii))
                ultralight.writePage(6, "ijkl".toByteArray(usAscii))
                ultralight.writePage(7, "mnop".toByteArray(usAscii))
            }
        }
    }

    fun readTag(tag: Tag): String? {
        return MifareUltralight.get(tag)?.use { mifare ->
            mifare.connect()
            val payload = mifare.readPages(4)
            String(payload, Charset.forName("US-ASCII"))
        }
    }

}