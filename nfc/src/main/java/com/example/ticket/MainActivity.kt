package com.example.ticket

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ticket.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {

    private var counter: Int = 0
    private var nfcAdapter: NfcAdapter? = null
    private val message = MutableStateFlow("Scan a tag:")
    private val records = MutableStateFlow<List<NdefRecord>?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ContentView()
        }
        if (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED) {
            message.value = "Discovered tag ${++counter} with intent: ${intent.data}"
        }
        message.value += intent.dataString ?: ""
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            message.value += "NFC is not available"
            return
        }
        if (nfcAdapter?.isEnabled != true) {
            message.value += "NFC is not enable"
            return
        }
    }

    @Composable
    fun ContentView() {
        MyApplicationTheme {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                Column(
                    Modifier
                        .padding(innerPadding)
                        .padding(vertical = 20.dp)
                ) {
                    val msg by message.collectAsState()
                    val records by records.collectAsState()
                    Text(msg)
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(10.dp),
                    ) {
                        itemsIndexed(records ?: emptyList()) { index, record ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, shape = RoundedCornerShape(15.dp))
                                    .padding(10.dp),
                            ) {
                                Text(text = "记录${index + 1}")
                                record.toMimeType()
                                Text(text = "地址: ${record.toUri()}")
                                val query = record.toUri().encodedQuery
                                if (query.isNullOrEmpty()) {
                                    Text(text = String(record.payload, Charset.forName("UTF-8")))
                                } else {
                                    Text(text = "参数: $query")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(
            this,
            PendingIntent.getActivity(
                this,
                1,
                Intent(this, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_MUTABLE
            ),
            null,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        message.value = listOf(
            "Discovered tag ${++counter} with",
            "action: ${intent.action}",
            "data: ${intent.dataString}"
        ).joinToString("\n")

        intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.forEach { msg ->
            (msg as? NdefMessage)?.records?.joinToString("\n") { record ->
                println(String(record.payload, Charset.forName("UTF-8")))
                record.toUri()?.toString() ?: ""
            }?.let { println("NdefMessage:\n$it") }
        }

        intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)?.let {
            Ndef.get(it)
        }?.runCatching {
            connect()
            records.value = ndefMessage.records?.toList()
        }
    }

    private fun handleNdefMessage(tag: Tag) {
        try {
            val ndef = Ndef.get(tag) ?: return
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