package com.jarvis.app.util

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS handles back navigation via UINavigationController swipe gesture,
    // which is managed by the platform. No-op in Compose Multiplatform.
}
