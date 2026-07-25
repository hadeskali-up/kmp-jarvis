package com.jarvis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jarvis.app.di.initKoin
import com.jarvis.app.widget.ForexWidgetWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initKoin()
        enableEdgeToEdge()
        // Schedule background widget refresh (every 15 min)
        ForexWidgetWorker.schedule(this)
        setContent {
            App()
        }
    }
}
