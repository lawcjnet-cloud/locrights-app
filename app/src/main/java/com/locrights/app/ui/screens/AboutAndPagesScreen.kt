package com.locrights.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.locrights.app.R
import com.locrights.app.data.local.ThemeMode
import com.locrights.app.ui.components.LocRightsBrandMark
import com.locrights.app.ui.components.NativeHtmlRenderer
import com.locrights.app.ui.viewmodel.AboutUiState
import com.locrights.app.util.ShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    uiState: AboutUiState,
    themeMode: ThemeMode,
    fontScale: Float,
    onChangeThemeMode: (ThemeMode) -> Unit,
    onRetryPages: () -> Unit,
    onOpenPrivacy: () -> Unit
) {
    val context = LocalContext.current
    val aboutPage = uiState.pages.firstOrNull {
        it.title.contains("About", ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_about)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        LocRightsBrandMark(showSubtitle = true)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Official reader for Law Order and Civil Rights (locrights-23.blogspot.com), an educational initiative led by Dr. Venudhar Routiya and Chief Editor Smt. Ratna Routiya.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(onClick = { ShareUtils.openExternalUrl(context, ShareUtils.WEBSITE_URL) }) {
                                Icon(Icons.Outlined.Public, contentDescription = null)
                                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                Text(stringResource(R.string.open_website))
                            }
                            OutlinedButton(onClick = onOpenPrivacy) {
                                Text(stringResource(R.string.menu_privacy_policy))
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Appearance",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = themeMode == ThemeMode.SYSTEM,
                                onClick = { onChangeThemeMode(ThemeMode.SYSTEM) },
                                label = { Text("System") }
                            )
                            FilterChip(
                                selected = themeMode == ThemeMode.LIGHT,
                                onClick = { onChangeThemeMode(ThemeMode.LIGHT) },
                                leadingIcon = { Icon(Icons.Outlined.LightMode, contentDescription = null) },
                                label = { Text("Light") }
                            )
                            FilterChip(
                                selected = themeMode == ThemeMode.DARK,
                                onClick = { onChangeThemeMode(ThemeMode.DARK) },
                                leadingIcon = { Icon(Icons.Outlined.DarkMode, contentDescription = null) },
                                label = { Text("Dark") }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Public Contact Details",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("ratnaroutiya@gmail.com", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(Icons.Outlined.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("+91 9752024287 (WhatsApp)", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "About Law Order and Civil Rights",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(12.dp))
                        when {
                            uiState.isLoading -> CircularProgressIndicator()
                            aboutPage != null -> {
                                NativeHtmlRenderer(
                                    html = aboutPage.contentHtml,
                                    fontScale = fontScale,
                                    onLinkClick = { url -> ShareUtils.openExternalUrl(context, url) }
                                )
                            }
                            else -> {
                                Text(
                                    text = "Dr. Venudhar Routiya (Assistant Professor, School of Studies in Law, Pt. Ravishankar Shukla University, Raipur, C.G.) leads this platform to provide free legal education, UGC NET/JRF study notes, Civil Judge & ADPO guidance, and current affairs.",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (uiState.errorMessage != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedButton(onClick = onRetryPages) {
                                        Text(stringResource(R.string.retry))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaticBlogPageScreen(
    title: String,
    pageTitleMatcher: String,
    uiState: AboutUiState,
    fontScale: Float,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val page = uiState.pages.firstOrNull {
        it.title.contains(pageTitleMatcher, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                title = { Text(page?.title ?: title) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp)
        ) {
            item {
                when {
                    uiState.isLoading -> CircularProgressIndicator()
                    page != null -> {
                        NativeHtmlRenderer(
                            html = page.contentHtml,
                            fontScale = fontScale,
                            onLinkClick = { url -> ShareUtils.openExternalUrl(context, url) }
                        )
                    }
                    else -> {
                        Text(
                            text = "This page can also be viewed directly on the official website.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { ShareUtils.openExternalUrl(context, ShareUtils.WEBSITE_URL) }) {
                            Text(stringResource(R.string.open_website))
                        }
                    }
                }
            }
        }
    }
}
