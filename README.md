# Loc Rights — Native Android Application (Kotlin & Jetpack Compose)

Official native Android reader for **Law Order and Civil Rights** (`https://locrights-23.blogspot.com/`), built with Kotlin, Jetpack Compose, Material 3, MVVM, Retrofit, Coil, Room, and DataStore.

## Key Specifications
- **Application Name**: Loc Rights (`लॉक राइट्स` in Hindi locale)
- **Package Name**: `com.locrights.app`
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target / Compile SDK**: 35 (Android 15)
- **Architecture**: MVVM + Repository pattern + Kotlin Coroutines & StateFlow
- **Data Source**: Official Blogger public JSON feeds:
  - Posts: `https://locrights-23.blogspot.com/feeds/posts/default?alt=json`
  - Category Filter: `https://locrights-23.blogspot.com/feeds/posts/default/-/{label}?alt=json`
  - Pages (About Us, Contact, Privacy Policy): `https://locrights-23.blogspot.com/feeds/pages/default?alt=json`

## Implemented Features
1. **Animated Brand Splash Screen** with Loc Rights emblem & smooth transition.
2. **Home Feed** with Featured Lead Story, Latest Articles, real Blogger category chips, pull-to-refresh, and reliable offset pagination (`start-index`).
3. **Categories Browser** automatically populated from Blogger feed labels (`feed.category`).
4. **Native Article Reader** (`SelectionContainer` for text selection & copy, Coil images, headings, paragraphs, lists, blockquotes, clickable links, adjustable font size, share intent, bookmarking, and related articles — no WebView wrapper).
5. **Instant Search** across Blogger titles & content with real label/recent suggestions and empty state.
6. **Persistent Bookmarks & Offline Cache** powered by Room (`LocRightsDatabase`).
7. **Navigation**: 4-tab Material 3 Bottom Navigation (`Home`, `Categories`, `Bookmarks`, `About`) + Modal Navigation Drawer (`Home`, `Latest Articles`, `Categories`, `Saved Articles`, `About Loc Rights`, `Privacy Policy`, `Contact Us`, `Share App`, `Rate App`).
8. **Bilingual Hindi & English Support**: `values/strings.xml` & `values-hi/strings.xml`, full Devanagari rendering.
9. **Light & Dark Themes** persisted via DataStore Preferences.
10. **Notification-Ready Architecture**: `NotificationHelper` creates Android O+ notification channels and handles `POST_NOTIFICATIONS` permissions gracefully. Push notifications require adding your Firebase project's `google-services.json` and enabling `firebase-messaging` in `app/build.gradle.kts` (no fake notifications are generated).

## Opening & Running in Android Studio
1. Open `/android-project` in **Android Studio Ladybug (2024.2+)** or newer.
2. Let Gradle sync dependencies (AGP 8.7.3, Kotlin 2.0.21, Compose BOM 2024.11.00).
3. Run on an Android 8.0+ (API 26+) device or emulator.
