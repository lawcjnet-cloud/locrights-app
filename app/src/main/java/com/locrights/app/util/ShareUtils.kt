package com.locrights.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent

object ShareUtils {
    const val WEBSITE_URL = "https://locrights-23.blogspot.com/"

    fun shareArticle(context: Context, title: String, url: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, "$title\n\nRead on Law Order and Civil Rights:\n$url")
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Article"))
    }

    fun shareApp(context: Context) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Loc Rights — Law Order and Civil Rights")
            putExtra(
                Intent.EXTRA_TEXT,
                "Read legal updates, Supreme Court judgments, and UGC NET / Judiciary study guides on Loc Rights:\n$WEBSITE_URL"
            )
        }
        context.startActivity(Intent.createChooser(sendIntent, "Share Loc Rights"))
    }

    fun openExternalUrl(context: Context, url: String) {
        val safeUri = runCatching { Uri.parse(url) }.getOrNull() ?: return
        runCatching {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
            customTabsIntent.launchUrl(context, safeUri)
        }.onFailure {
            val fallback = Intent(Intent.ACTION_VIEW, safeUri)
            context.startActivity(fallback)
        }
    }

    fun openPlayStoreListing(context: Context) {
        val packageName = context.packageName.removeSuffix(".debug")
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        runCatching {
            context.startActivity(marketIntent)
        }.onFailure {
            openExternalUrl(context, "https://play.google.com/store/apps/details?id=$packageName")
        }
    }
}
