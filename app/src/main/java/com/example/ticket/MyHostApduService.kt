package com.example.ticket

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class MyHostApduService : HostApduService() {

    private var messageCounter: Int = 0

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if (selectAidApdu(commandApdu)) {
            println("Application selected")
            return "Hello Desktop".toByteArray()
        } else {
            println("Received ${commandApdu?.toString()}")
            return "Message from android: ${messageCounter++}".toByteArray()
        }
    }

    override fun onDeactivated(reason: Int) {
        TODO("Not yet implemented")
    }

    private fun selectAidApdu(commandApdu: ByteArray?): Boolean {
        return commandApdu?.let {
            it.size >= 2 && it[0] == 0.toByte() && it[1] == 0xa4.toByte()
        } ?: false
    }
}