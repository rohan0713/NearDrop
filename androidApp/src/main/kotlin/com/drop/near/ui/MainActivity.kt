package com.drop.near.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.drop.near.ui.devicelist.DeviceListScreen
import com.drop.near.ui.theme.NearDropTheme
import com.drop.near.ui.transfer.TransferHistoryScreen
import com.drop.near.di.NearDropSdk

enum class Screen {
    DEVICE_LIST,
    TRANSFERS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NearDropTheme {
                NearDropNavHost()
            }
        }
    }
}

@Composable
fun NearDropNavHost() {
    var currentScreen by remember { mutableStateOf(Screen.DEVICE_LIST) }
    val deviceListViewModel = remember { NearDropSdk.container.createDeviceListViewModel() }
    val transferViewModel = remember { NearDropSdk.container.createTransferViewModel() }

    when (currentScreen) {
        Screen.DEVICE_LIST -> {
            DeviceListScreen(
                viewModel = deviceListViewModel,
                transferViewModel = transferViewModel,
                onNavigateToTransfers = { currentScreen = Screen.TRANSFERS }
            )
        }
        Screen.TRANSFERS -> {
            TransferHistoryScreen(
                viewModel = transferViewModel,
                onNavigateBack = { currentScreen = Screen.DEVICE_LIST }
            )
        }
    }
}
