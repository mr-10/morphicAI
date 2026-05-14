package com.mr10.morphicai.ui.components

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MorphicWebView(
    url: String,
    modifier: Modifier = Modifier,
    onWebViewCreated: (WebView) -> Unit = {}
) {
    var isLoading by remember { mutableStateOf(true) }
    var isOffline by remember { mutableStateOf(false) }
    var webView: WebView? by remember { mutableStateOf(null) }
    var currentFilePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        currentFilePathCallback?.onReceiveValue(if (uris.isNotEmpty()) uris.toTypedArray() else null)
        currentFilePathCallback = null
    }

    BackHandler(enabled = webView?.canGoBack() == true && !isOffline) {
        webView?.goBack()
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (isOffline) {
            OfflineScreen(onRetry = {
                isOffline = false
                isLoading = true
                webView?.reload()
            })
        } else {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        this.layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        
                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                        setLayerType(View.LAYER_TYPE_HARDWARE, null)
                        
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            cacheMode = WebSettings.LOAD_DEFAULT
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            
                            // Set paths for persistence
                            databasePath = context.getDir("databases", 0).path
                            
                            @Suppress("DEPRECATION")
                            setRenderPriority(WebSettings.RenderPriority.HIGH)
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                isOffline = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    isOffline = true
                                    isLoading = false
                                }
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                if (newProgress > 70 && !isOffline) {
                                    isLoading = false
                                }
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                currentFilePathCallback?.onReceiveValue(null)
                                currentFilePathCallback = filePathCallback
                                
                                val mimeTypes = fileChooserParams?.acceptTypes ?: arrayOf("*/*")
                                try {
                                    filePickerLauncher.launch(mimeTypes)
                                } catch (e: Exception) {
                                    currentFilePathCallback?.onReceiveValue(null)
                                    currentFilePathCallback = null
                                    return false
                                }
                                return true
                            }
                        }

                        loadUrl(url)
                        webView = this
                        onWebViewCreated(this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                SkeletonLoader()
            }
        }
    }
}
