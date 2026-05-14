package com.mr10.morphicai

import android.os.Bundle
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.mr10.morphicai.ui.components.MorphicWebView
import com.mr10.morphicai.ui.theme.MorphicAiTheme

class MainActivity : ComponentActivity() {
    private var webView: WebView? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, true)
        
        setContent {
            MorphicAiTheme {
                val statusBarPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { innerPadding ->
                    MorphicWebView(
                        url = "https://chat.morphic.sh",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = statusBarPadding)
                            .padding(bottom = innerPadding.calculateBottomPadding()),
                        onWebViewCreated = { webView = it }
                    )
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        webView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView?.onResume()
    }

    override fun onDestroy() {
        webView?.destroy()
        webView = null
        super.onDestroy()
    }
}
