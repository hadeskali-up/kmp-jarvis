package com.jarvis.app.di

import com.jarvis.app.screens.DashboardViewModel
import com.jarvis.app.services.AiUsageService
import com.jarvis.app.services.CryptoService
import com.jarvis.app.services.DashboardService
import com.jarvis.app.services.ExpenseDatabase
import com.jarvis.app.services.ForexService
import com.jarvis.app.services.ProviderBalanceService
import com.jarvis.app.services.TradeHistoryService
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule: Module = module {
    // Services
    single { DashboardService() }
    single { AiUsageService() }
    single { CryptoService() }
    single { ForexService() }
    single { TradeHistoryService() }
    single { ProviderBalanceService() }
    single { ExpenseDatabase() }

    // ViewModels
    viewModelOf(::DashboardViewModel)
}
