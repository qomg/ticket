package com.example.ticket

import android.content.Intent
import android.nfc.cardemulation.OffHostApduService
import android.os.IBinder

class MyOffHostApduService : OffHostApduService() {
    override fun onBind(intent: Intent?): IBinder? {
        println("${intent?.action}")
        return null
    }
}