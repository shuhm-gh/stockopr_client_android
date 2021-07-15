package com.example.stockopr

import android.app.Service
import android.content.Intent

import android.os.IBinder

import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener

import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener
import android.util.Log
import android.widget.Toast
import java.util.*


class TTS : Service(), OnInitListener, OnUtteranceCompletedListener {
    private val TAG = "TTS"
    private var mTts: TextToSpeech? = null
    private var spokenText: String? = null
    override fun onCreate() {
        Log.i(TAG, "on create")
        mTts = TextToSpeech(this, this)
        // This is a good place to set spokenText
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(applicationContext, "enter onStartCommand", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "enter onStartCommand")
        if (intent != null) {
            spokenText = intent.getStringExtra("content")
            spokenText?.let { Log.i(TAG, it) }
        }
        // Log.i(TAG, "Service onStartCommand " + startId)
        return Service.START_STICKY
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = mTts!!.setLanguage(Locale.CHINESE)
            if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                mTts!!.speak(spokenText, TextToSpeech.QUEUE_FLUSH, null)
            }
        }
    }

    override fun onUtteranceCompleted(uttId: String) {
        stopSelf()
    }

    override fun onDestroy() {
        if (mTts != null) {
            mTts!!.stop()
            mTts!!.shutdown()
        }
        super.onDestroy()
    }

    override fun onBind(arg0: Intent?): IBinder? {
        return null
    }
}