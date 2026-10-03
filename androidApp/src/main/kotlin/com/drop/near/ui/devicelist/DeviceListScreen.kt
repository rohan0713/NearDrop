package com.drop.near.ui.devicelist

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Tablet
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drop.near.bluetooth.BluetoothManagerHelper
import com.drop.near.file.GalleryFileHelper
import com.drop.near.file.SelectedFile
import com.drop.near.service.BluetoothTransferService
import com.drop.near.domain.model.Device
import com.drop.near.domain.model.DeviceType
import com.drop.near.domain.model.SignalStrength
import com.drop.near.domain.model.TransferItem
import com.drop.near.domain.model.TransferStatus
import com.drop.near.presentation.devicelist.DeviceListEffect
import com.drop.near.presentation.devicelist.DeviceListIntent
import com.drop.near.presentation.devicelist.DeviceListViewModel
import com.drop.near.presentation.transfer.TransferViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceListScreen(
    viewModel: DeviceListViewModel,
    transferViewModel: TransferViewModel,
    onNavigateToTransfers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val transferState by transferViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedFile by remember { mutableStateOf<SelectedFile?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Paired, 1: Nearby Scanned
    var hasPermissions by remember { mutableStateOf(BluetoothManagerHelper.hasRequiredPermissions(context)) }
    var isBluetoothEnabled by remember { mutableStateOf(BluetoothManagerHelper.isBluetoothEnabled(context)) }
    var pairedBluetoothDevices by remember { mutableStateOf<List<Device>>(emptyList()) }
    var isScanningBluetooth by remember { mutableStateOf(false) }

    // Gallery Photo/Video Picker launcher
    val galleryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedFile = GalleryFileHelper.resolveFile(context, uri)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Selected: ${selectedFile?.name}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // General File Picker launcher (Documents, ZIP, any file)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedFile = GalleryFileHelper.resolveFile(context, uri)
            coroutineScope.launch {
                snackbarHostState.showSnackbar(
                    message = "Selected: ${selectedFile?.name}",
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    // Permission Request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        hasPermissions = BluetoothManagerHelper.hasRequiredPermissions(context)
        isBluetoothEnabled = BluetoothManagerHelper.isBluetoothEnabled(context)
        if (hasPermissions && isBluetoothEnabled) {
            pairedBluetoothDevices = BluetoothManagerHelper.getPairedDevices(context)
        }
    }

    // Refresh paired devices on start
    LaunchedEffect(hasPermissions, isBluetoothEnabled) {
        if (hasPermissions && isBluetoothEnabled) {
            pairedBluetoothDevices = BluetoothManagerHelper.getPairedDevices(context)
        }
    }

    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onCleared()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DeviceListEffect.ShowToast -> {
                    snackbarHostState.showSnackbar(effect.message)
                }
                is DeviceListEffect.OpenSendDialog -> {
                    // Handled directly via BluetoothTransferService
                }
                is DeviceListEffect.NavigateToTransfer -> {
                    onNavigateToTransfers()
                }
            }
        }
    }

    val activeTransfer = transferState.transfers.firstOrNull { !it.status.isFinished }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NearDrop",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Bluetooth 1.0",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        BluetoothStatusPill(
                            isEnabled = isBluetoothEnabled,
                            hasPermissions = hasPermissions,
                            onRequestPermissions = {
                                permissionLauncher.launch(BluetoothManagerHelper.getRequiredPermissions())
                            }
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (!hasPermissions) {
                                permissionLauncher.launch(BluetoothManagerHelper.getRequiredPermissions())
                            } else {
                                isScanningBluetooth = true
                                pairedBluetoothDevices = BluetoothManagerHelper.getPairedDevices(context)
                                viewModel.onIntent(DeviceListIntent.Scan)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Scanning for nearby Bluetooth devices...")
                                }
                            }
                        },
                        enabled = !state.isScanning
                    ) {
                        if (state.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Scan devices"
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToTransfers) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Transfer History"
                            )
                            if (transferState.activeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color(0xFF10B981), CircleShape)
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Banner (if missing)
            AnimatedVisibility(
                visible = !hasPermissions,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                PermissionRequestCard(
                    onRequestPermissions = {
                        permissionLauncher.launch(BluetoothManagerHelper.getRequiredPermissions())
                    }
                )
            }

            // Active Transfer In-App Progress Banner
            AnimatedVisibility(
                visible = activeTransfer != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                activeTransfer?.let { transfer ->
                    ActiveTransferCard(
                        transfer = transfer,
                        onCancel = {
                            BluetoothTransferService.cancelTransfer(context, transfer.id)
                        }
                    )
                }
            }

            // Gallery File Selection Section
            GalleryFilePickerCard(
                selectedFile = selectedFile,
                onPickFromGallery = {
                    galleryPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onPickOtherFile = {
                    documentPickerLauncher.launch("*/*")
                },
                onClearFile = {
                    selectedFile = null
                }
            )

            // Segmented Device View Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BluetoothConnected,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Paired (${pairedBluetoothDevices.size})")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nearby (${state.displayedDevices.size})")
                        }
                    }
                )
            }

            // Search Bar for Devices
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onIntent(DeviceListIntent.UpdateSearch(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                placeholder = { Text("Filter devices by name or address...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onIntent(DeviceListIntent.UpdateSearch("")) }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )

            // Device List Content
            Box(modifier = Modifier.fillMaxSize()) {
                val devicesToShow = if (selectedTab == 0) {
                    if (state.searchQuery.isBlank()) {
                        pairedBluetoothDevices
                    } else {
                        pairedBluetoothDevices.filter {
                            it.name.contains(state.searchQuery, ignoreCase = true) ||
                                    it.id.contains(state.searchQuery, ignoreCase = true)
                        }
                    }
                } else {
                    state.displayedDevices
                }

                if (devicesToShow.isEmpty()) {
                    EmptyBluetoothDevicesView(
                        isPairedTab = selectedTab == 0,
                        searchQuery = state.searchQuery,
                        onScan = {
                            if (!hasPermissions) {
                                permissionLauncher.launch(BluetoothManagerHelper.getRequiredPermissions())
                            } else {
                                viewModel.onIntent(DeviceListIntent.Scan)
                                pairedBluetoothDevices = BluetoothManagerHelper.getPairedDevices(context)
                            }
                        },
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(devicesToShow, key = { it.id }) { device ->
                            BluetoothDeviceCard(
                                device = device,
                                isReadyToSend = selectedFile != null,
                                onSend = {
                                    val currentFile = selectedFile
                                    if (currentFile != null) {
                                        BluetoothTransferService.startTransfer(
                                            context = context,
                                            targetDevice = device,
                                            fileName = currentFile.name,
                                            fileSizeBytes = currentFile.sizeBytes,
                                            fileUri = currentFile.uri,
                                            mimeType = currentFile.mimeType
                                        )
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Sharing \"${currentFile.name}\" to ${device.name}. Check your notification drawer for live status.",
                                                duration = SnackbarDuration.Long
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Please select a file from Gallery first.",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                        galleryPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    }
                                },
                                onToggleFavorite = {
                                    viewModel.onIntent(DeviceListIntent.ToggleFavorite(device.id))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BluetoothStatusPill(
    isEnabled: Boolean,
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 2.dp)
    ) {
        val (dotColor, statusText) = when {
            !hasPermissions -> Color(0xFFF59E0B) to "Permissions Needed"
            isEnabled -> Color(0xFF10B981) to "Bluetooth Ready"
            else -> Color(0xFFEF4444) to "Bluetooth Disabled"
        }

        Box(
            modifier = Modifier
                .size(7.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (!hasPermissions) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Grant",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onRequestPermissions)
            )
        }
    }
}

@Composable
fun GalleryFilePickerCard(
    selectedFile: SelectedFile?,
    onPickFromGallery: () -> Unit,
    onPickOtherFile: () -> Unit,
    onClearFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selectedFile != null) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        )
    ) {
        if (selectedFile == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Select File to Share",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Pick photos, videos, or documents to send via Bluetooth",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPickFromGallery,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Gallery")
                    }
                    OutlinedButton(
                        onClick = onPickOtherFile,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse Files")
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when {
                            selectedFile.isImage -> Icons.Default.Image
                            selectedFile.isVideo -> Icons.Default.VideoFile
                            else -> Icons.AutoMirrored.Filled.InsertDriveFile
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedFile.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = selectedFile.formattedSize,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = selectedFile.mimeType?.substringAfter('/')?.uppercase() ?: "FILE",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onPickFromGallery) {
                    Icon(
                        imageVector = Icons.Default.PhotoLibrary,
                        contentDescription = "Change file",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onClearFile) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear file",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTransferCard(
    transfer: TransferItem,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Transferring: ${transfer.fileName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "To ${transfer.device.name} • Status in notification drawer",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${transfer.progressPercent}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(onClick = onCancel, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel transfer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { transfer.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }
    }
}

@Composable
fun BluetoothDeviceCard(
    device: Device,
    isReadyToSend: Boolean,
    onSend: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSend),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getDeviceIcon(device.type),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = device.id.takeLast(11),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SignalBadge(signalStrength = device.signalStrength)
                }
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (device.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Toggle favorite",
                    tint = if (device.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onSend,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReadyToSend) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    contentColor = if (isReadyToSend) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isReadyToSend) "Send" else "Share",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
fun PermissionRequestCard(
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Bluetooth & Notifications",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Grant permissions to discover Bluetooth devices and track transfers in notifications.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermissions,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Allow", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun EmptyBluetoothDevicesView(
    isPairedTab: Boolean,
    searchQuery: String,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isPairedTab) Icons.Default.BluetoothConnected else Icons.Default.Bluetooth,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = if (searchQuery.isNotEmpty()) {
                "No devices match \"$searchQuery\""
            } else if (isPairedTab) {
                "No Paired Bluetooth Devices"
            } else {
                "No Nearby Devices Found"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isPairedTab) {
                "Pair your phone with another device in Android Settings, or scan for nearby devices."
            } else {
                "Make sure Bluetooth is turned on and the recipient device is discoverable."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onScan,
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Scan for Devices")
        }
    }
}

@Composable
fun SignalBadge(signalStrength: SignalStrength) {
    val (label, color) = when (signalStrength) {
        SignalStrength.EXCELLENT -> "Strong" to Color(0xFF10B981)
        SignalStrength.GOOD -> "Good" to Color(0xFF3B82F6)
        SignalStrength.FAIR -> "Fair" to Color(0xFFF59E0B)
        SignalStrength.POOR -> "Weak" to Color(0xFFEF4444)
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
        )
    }
}

fun getDeviceIcon(type: DeviceType): ImageVector {
    return when (type) {
        DeviceType.PHONE -> Icons.Default.PhoneAndroid
        DeviceType.TABLET -> Icons.Default.Tablet
        DeviceType.LAPTOP -> Icons.Default.Laptop
        DeviceType.DESKTOP -> Icons.Default.Computer
        DeviceType.UNKNOWN -> Icons.Default.Devices
    }
}
