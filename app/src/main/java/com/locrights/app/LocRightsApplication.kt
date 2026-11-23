package com.locrights.app

import android.app.Application
import com.locrights.app.data.local.LocRightsDatabase
import com.locrights.app.data.local.ThemePreferences
import com.locrights.app.data.remote.NetworkModule
import com.locrights.app.data.repository.BlogRepository
import com.locrights.app.notifications.NotificationHelper
import com.locrights.app.util.NetworkMonitor

class LocRightsApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    private val database = LocRightsDatabase.getInstance(application)
    private val apiService = NetworkModule.createBloggerApi(application)

    val themePreferences = ThemePreferences(application)
    val networkMonitor = NetworkMonitor(application)
    val blogRepository = BlogRepository(
        api = apiService,
        bookmarkDao = database.bookmarkDao(),
        cachedArticleDao = database.cachedArticleDao()
    )
}
