package com.jarvis.app.di

import org.koin.core.context.startKoin
import org.koin.core.KoinApplication
import org.koin.core.context.KoinContext

/**
 * Call once from each platform entry point (MainActivity, MainViewController).
 * Idempotent — safe to call multiple times.
 */
fun initKoin(appDeclaration: (KoinApplication.() -> Unit) = {}): KoinApplication {
    return startKoin {
        modules(appModule)
        appDeclaration()
    }
}
