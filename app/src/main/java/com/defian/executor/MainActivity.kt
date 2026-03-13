package com.defian.executor

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.defian.executor.ui.theme.DefianExecutorTheme

class MainActivity : ComponentActivity() {

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (Settings.canDrawOverlays(this)) {
            startOverlayService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DefianExecutorTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExecutorControlPanel(
                        modifier = Modifier.padding(innerPadding),
                        onStartExecutor = { checkAndStartOverlay() }
                    )
                }
            }
        }
    }

    private fun checkAndStartOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            startOverlayService()
        }
    }

    private fun startOverlayService() {
        val serviceIntent = Intent(this, OverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}

@Composable
fun ExecutorControlPanel(modifier: Modifier = Modifier, onStartExecutor: () -> Unit) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(text = "Defian Executor", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "The ultimate Minecraft Bedrock executor. Inject scripts, create custom UIs, and dominate servers.")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStartExecutor) {
            Text("Launch Floating Executor")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Status: Ready", style = MaterialTheme.typography.bodySmall)
    }
}
