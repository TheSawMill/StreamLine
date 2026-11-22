package com.streamline.tv

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.streamline.tv.ui.StreamlineApp
import com.streamline.tv.ui.home.HomeViewModel
import com.streamline.tv.ui.theme.StreamlineTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            StreamlineTheme {
                StreamlineApp(viewModel)
            }
        }
    }
}
