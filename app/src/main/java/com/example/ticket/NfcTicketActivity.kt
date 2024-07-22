package com.example.ticket

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ticket.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.charset.Charset

class NfcTicketActivity : ComponentActivity() {

    private var counter: Int = 0
    private var nfcAdapter: NfcAdapter? = null
    private val result = MutableStateFlow("Scan a tag:")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            result.value += "Discovered tag ${++counter} with intent: ${intent.data}"
        }
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        val text by result.collectAsState()
                        Text(text = text)
                    }
                }
            }
        }
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_SHORT).show()
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            Toast.makeText(this, "NFC is not enable", Toast.LENGTH_SHORT).show()
            return
        }
    }

    private val buildIntent: Intent by lazy {
        Intent(this, NfcTicketActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    private val filters: Array<IntentFilter> by lazy {
        arrayOf(
            IntentFilter.create(NfcAdapter.ACTION_NDEF_DISCOVERED, "*/*"),
            IntentFilter.create(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*"),
        )
    }

    private val techList: Array<Array<String>> by lazy {
        arrayOf(
            arrayOf(
                NfcA::class.java.name,
                NfcB::class.java.name,
                NfcF::class.java.name,
                NfcV::class.java.name,
            )
        )
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            PendingIntent.getActivity(
                this,
                1,
                buildIntent,
                PendingIntent.FLAG_MUTABLE
//                FLAG_ONE_SHOT or FLAG_IMMUTABLE
            ),
            null, //filters,
            null, //techList
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        result.value += "Discovered tag ${++counter} with intent: ${intent.data}"
        if (intent.action != NfcAdapter.ACTION_NDEF_DISCOVERED || intent.action != NfcAdapter.ACTION_TECH_DISCOVERED) return

        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.forEach { msg ->
            (msg as? NdefMessage)?.records?.forEach { record ->
                println(record.toUri())
            }
        }

        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

        if (tag!= null) {

            try {
                val ndef = Ndef.get(tag)
                ndef.connect()

                val payload = ndef.getNdefMessage().records[0].payload

                // 解析数据，假设前 1 个字节表示操作类型，后面是相关数据
                val operationType = payload[0].toInt()

                when (operationType) {
                    0x01 -> { // 充值
                        val amountToRecharge = byteArrayToInt(payload, 1, 4) // 假设接下来 4 个字节表示充值金额
                        rechargeTicket(amountToRecharge)
                    }
                    0x02 -> { // 查询余额
                        queryBalance()
                    }
                    0x03 -> { // 查询交易记录
                        queryTransactionRecords()
                    }
                }

                ndef.close()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    // 将字节数组转换为整数的方法
    private fun byteArrayToInt(bytes: ByteArray, start: Int, length: Int): Int {
        var value = 0
        for (i in start until start + length) {
            value = (value shl 8) + (bytes[i].toInt() and 0xFF)
        }
        return value
    }

    // 充值逻辑
    private fun rechargeTicket(amount: Int) {
        // 这里模拟充值成功
        Log.d("NFC", "成功充值 $amount 元")
    }

    // 查询余额逻辑
    private fun queryBalance() {
        val balance = 50 // 这里模拟一个余额值
        Log.d("NFC", "当前余额: $balance 元")
    }

    // 查询交易记录逻辑
    private fun queryTransactionRecords() {
        val transactionRecords = arrayOf("交易 1: 消费 10 元", "交易 2: 充值 20 元") // 模拟交易记录
        for (record in transactionRecords) {
            Log.d("NFC", record)
        }
    }

    fun writeTag(tag: Tag, tagText: String) {
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