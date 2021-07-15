package com.example.stockopr

// import kotlinx.serialization.json.Json
// import kotlinx.serialization.Serializable
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.*
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


@Serializable
data class TradeSignal(
    var code: String, var name: String, var price: Double, var date: String, var command: String, var period: String) {
}

class MyService : Service(), TextToSpeech.OnInitListener {

    private val TAG = "ServiceExample"
    var running = true
    lateinit var mMessenger: Messenger
    // lateinit var mHandler: Handler
    private var mTts: TextToSpeech? = null
    private var spokenText: String? = null
    var intentTTS: Intent? = null

    var context: Context? = null

//    var tts = TextToSpeech(this) { status ->
//        if (status != TextToSpeech.ERROR) {
//            val result: Int = tts.setLanguage(mLanguage)
//            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                Log.e("Text2SpeechWidget", "$result is not supported")
//            }
//        }
//    }

    override fun onCreate() {
        super.onCreate()
        context = this
        Log.i(TAG, "Service onCreate")
        mTts = TextToSpeech(this, this)
        var notification = createNotification()
        startForeground(1, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext,"enter onStartCommand",Toast.LENGTH_SHORT).show()

        // Log.i(TAG, "Service onStartCommand " + startId)

        // if (mMessenger == null) {
            if (intent != null) {
                mMessenger = intent.getExtras()?.get("messenger") as Messenger
            }
        // }

        try {
            checkMessage()
        } catch (e: Exception) {
        } catch (e: Exception) {
            // Toast.makeText(applicationContext, e.message, Toast.LENGTH_LONG).show()
            Log.e(TAG,  e.getStackTrace().toString())
        }

        // Toast.makeText(applicationContext,"will return",Toast.LENGTH_SHORT).show()
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        // Log.i(TAG, "Service onBind")
        return null
    }

    override fun onDestroy() {
        running = false
        Toast.makeText(applicationContext,"enter onDestroy",Toast.LENGTH_SHORT).show()
        Log.i(TAG, "Service onDestroy")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.i(TAG, "TTS Ok")
            Toast.makeText(applicationContext,"TTS OK",Toast.LENGTH_SHORT).show()
            val result = mTts!!.setLanguage(Locale.CHINESE)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                // mTts!!.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null)
            }
        } else {
            Toast.makeText(applicationContext,"TTS FAILED",Toast.LENGTH_SHORT).show()
            Log.i("TTS", status.toString())
        }
    }

    fun checkMessage() {
        Thread(object : Runnable {
            override fun run() {
                while (running) {
                    Log.i(TAG, "Service running")
                    // Toast.makeText(applicationContext, "in while", Toast.LENGTH_SHORT).show()
                    // genMessage()
                    sendPostRequest("hello world")
                    Thread.sleep(1000*60)
                }
                Log.i(TAG, "checkMessage will exit")
            }
        }).start()
    }

    fun genMessage() {
        // serializing objects
        var signal = TradeSignal("300502", "", 30.0, "2021-07-15 11:25:00", "B", "day")
        val jsonData = Json.encodeToString(signal)
        println(jsonData) // {"a": 42, "b": "42"}

        // parsing data back
        val obj = Json.decodeFromString<TradeSignal>(jsonData)
        println(obj) // MyModel(a=42, b="42")
    }

    fun sendPostRequest(msg:String) {  // userName:String, password:String) {

        // var reqParam = URLEncoder.encode("username", "UTF-8") + "=" + URLEncoder.encode(userName, "UTF-8")
        // reqParam += "&" + URLEncoder.encode("password", "UTF-8") + "=" + URLEncoder.encode(password, "UTF-8")
        var reqParam = msg
        val mURL = URL("http://192.168.1.100:8080/api/query_trade_signal")

        with(mURL.openConnection() as HttpURLConnection) {
            // optional default is GET
            requestMethod = "POST"
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            val wr = OutputStreamWriter(getOutputStream());
            // wr.write(reqParam);
            wr.write("{\"date\": \"2021-07-15 15:00:00\"}")
            wr.flush();

            println("URL : $url")
            println("Response Code : $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }

                println("Response : $response")

                spokenText = "注意, 交易信号"
                mTts?.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null, null)

//                var bundleTTS: Bundle = Bundle()
//                bundleTTS.putString("trade_signal", response.toString())
//
//                intentTTS = Intent(applicationContext, TTS::class.java)
//                intentTTS!!.putExtra("content", "测试")
//                startService(intentTTS)

                var message = Message.obtain();
                message.what = 0
//                message.arg1 = response.toString();
//                message.arg2 = duration;
                var bundle: Bundle = Bundle()
                bundle.putString("trade_signal", response.toString())
                message.data = bundle
                // message.
                try {
                    mMessenger.send(message)
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "ENDLESS SERVICE CHANNEL"

        // depending on the Android API that we're dealing with we will have
        // to use a specific method to create the notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager;
            val channel = NotificationChannel(
                notificationChannelId,
                "Endless Service notifications channel",
                NotificationManager.IMPORTANCE_HIGH
            ).let {
                it.description = "Endless Service channel"
                it.enableLights(true)
                it.lightColor = Color.RED
                it.enableVibration(true)
                it.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                it
            }
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }

        val builder: Notification.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) Notification.Builder(
            this,
            notificationChannelId
        ) else Notification.Builder(this)

        return builder
            .setContentTitle("Endless Service")
            .setContentText("This is your favorite endless service working")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setTicker("Ticker text")
            // .setPriority(Notification.PRIORITY_HIGH) // for under android 26 compatibility
            .build()
    }
}