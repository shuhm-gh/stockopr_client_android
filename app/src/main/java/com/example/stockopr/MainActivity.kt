package com.example.stockopr

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var permissions = arrayOf(android.Manifest.permission.INTERNET)
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        }

        var tv: TextView = this.findViewById<Button>(R.id.tv)
        var intentTTS = Intent(this, TTS::class.java)

        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                var jsonStr = msg.data.getString("trade_signal")
                println(jsonStr)
                val signal_list =
                    jsonStr?.let {
                        Json { isLenient = true }.decodeFromString<List<TradeSignal>>(
                            it
                        )
                    }
                // print(signal_list)
                // Your logic code here.
                jsonStr = Json.encodeToString(signal_list)
                tv.setText(jsonStr);

                startService(intentTTS)
            }
        }

        val btnStartService = this.findViewById<Button>(R.id.btnStartService)
        btnStartService.setOnClickListener {
            var service = Intent(this, MyService::class.java)
            // service.putExtra("option", option)
            service.putExtra("messenger", Messenger(mHandler))
            // service.putExtra("path", path)
            // startService(service)
            startForegroundService(service)   // minSdkVersion
        }

        val btnStopService = this.findViewById<Button>(R.id.btnStopService)
        btnStopService.setOnClickListener {
            intent = Intent(this, MyService::class.java)
            stopService(intent)
        }



        val intent = Intent(this, MyIntentService::class.java)
        startService(intent)
    }
}