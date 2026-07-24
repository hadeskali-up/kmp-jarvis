package com.jarvis.app.di

import com.jarvis.app.screens.DashboardViewModel
import com.jarvis.app.services.DashboardService
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule: Module = module {
    // Services
    single { DashboardService() }

    // ViewModels
    viewModelOf(::DashboardViewModel)
}
