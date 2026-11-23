package com.locrights.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * Notification-ready architecture for Loc Rights.
 *
 * Channels are registered at app startup on Android 8.0+ (API 26+).
 * Push delivery is intentionally disabled until a real Firebase project
 * (`google-services.json` + `com.google.firebase:firebase-messaging`) is linked.
 */
object NotificationHelper {
    const val CHANNEL_ARTICLE_UPDATES = "loc_rights_article_updates"

    fun createNotificationChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ARTICLE_UPDATES,
            "Article & Judgment Alerts",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications when new articles or legal updates are published on Law Order and Civil Rights."
        }
        manager.createNotificationChannel(channel)
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Reports whether Firebase Cloud Messaging is configured in this build.
     * Returns false until `google-services.json` and FirebaseMessagingService are wired.
     */
    fun isPushBackendConfigured(): Boolean = false
}
