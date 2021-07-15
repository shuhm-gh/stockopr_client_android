package com.example.stockopr

import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService

class MyIntentService : JobIntentService() {

    private val TAG = "ServiceExample"

    // This method is called when service starts instead of onHandleIntent
    override fun onHandleWork(intent: Intent) {
        onHandleIntent(intent)
    }

    // remove override and make onHandleIntent private.
    private fun onHandleIntent(intent: Intent?) {}

    // convenient method for starting the service.
//    companion object {
//        fun enqueueWork(context: Context, intent: Intent) {
//            enqueueWork(context, FetchAddressIntentService::class.java, 1, intent)
//        }
//    }
}