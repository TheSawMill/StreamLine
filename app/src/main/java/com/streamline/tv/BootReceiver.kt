package com.streamline.tv

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper

/** Starts StreamLine after boot without depending on Fire OS launcher replacement. */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED && action != ACTION_QUICKBOOT) return

        // Fire OS rejects activity starts while it is still settling after boot.
        // goAsync() keeps the receiver alive long enough to retry once.
        val pendingResult = goAsync()
        Handler(Looper.getMainLooper()).postDelayed({
            try {
                launch(context)
            } finally {
                pendingResult.finish()
            }
        }, START_DELAY_MS)
    }

    private fun launch(context: Context) {
        val launchIntent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)

        runCatching {
            PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            ).send()
        }.onFailure {
            runCatching { context.startActivity(launchIntent) }
        }
    }

    private companion object {
        const val ACTION_QUICKBOOT = "android.intent.action.QUICKBOOT_POWERON"
        const val START_DELAY_MS = 3_000L
    }
}
