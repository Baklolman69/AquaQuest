package com.example.aquaquestai.presentation.screens.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.aquaquestai.data.ai.MlKitWaterValidator
import com.example.aquaquestai.data.model.EuropeanCity
import com.example.aquaquestai.presentation.components.CitySelectorBar
import com.example.aquaquestai.theme.*

@Composable
fun CameraScanScreen(
    selectedCity: EuropeanCity,
    onCitySelected: (EuropeanCity) -> Unit,
    onPhotoCaptured: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    val coroutineScope = rememberCoroutineScope()
    var isAnalyzing by remember { mutableStateOf(false) }
    var validationMsg by remember { mutableStateOf("Align camera with stream water surface") }
    var isFlashOn by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(OceanDarkBackground)
    ) {
        // Main Viewfinder Container
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp)
        ) {
            // City Switcher Bar
            CitySelectorBar(
                selectedCity = selectedCity,
                onCitySelected = onCitySelected
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(OceanCardSurface)
                    .border(2.dp, BioCyanPrimary.copy(alpha = 0.5f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    // Live CameraX Preview Feed
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                previewViewRef = this

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(surfaceProvider)
                                    }
                                    val selector = if (isFrontCamera) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }

                                    try {
                                        cameraProvider.unbindAll()
                                        cameraProvider.bindToLifecycle(
                                            lifecycleOwner,
                                            selector,
                                            preview
                                        )
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Permission Request Prompt State
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera Permission Required",
                            tint = BioCyanPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Camera Access Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryWhite,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Grant camera permission to enable real-time water body detection and spectral analysis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondaryMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { launcher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = BioCyanPrimary)
                        ) {
                            Text("Enable Camera", color = OceanDarkBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Grid Lines Overlay
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    HorizontalDivider(color = BioCyanPrimary.copy(alpha = 0.15f), thickness = 1.dp)
                    HorizontalDivider(color = BioCyanPrimary.copy(alpha = 0.15f), thickness = 1.dp)
                }

                // Center Frame Target Box
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .border(2.dp, BioCyanPrimary.copy(alpha = 0.8f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = BioCyanPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.FilterCenterFocus,
                            contentDescription = "Scan Target",
                            tint = BioCyanPrimary.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Top Validation Status Chip
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = GlassBackgroundDark
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isAnalyzing) WarningAmber else EmeraldHealthy)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = validationMsg,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimaryWhite
                        )
                    }
                }

                // Bottom Horizon Leveler
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f)
                ) {
                    Text(
                        text = "🌊 Computer Vision Target | ${selectedCity.riverSystem}",
                        style = MaterialTheme.typography.labelSmall,
                        color = BioCyanPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Capture Controls Area
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flash Toggle Button
                IconButton(
                    onClick = { isFlashOn = !isFlashOn },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (isFlashOn) BioCyanPrimary.copy(alpha = 0.3f) else OceanCardSurface,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (isFlashOn) "Flash On" else "Flash Off",
                        tint = if (isFlashOn) BioCyanPrimary else TextPrimaryWhite
                    )
                }

                // Real Camera Shutter Button
                Button(
                    onClick = {
                        isAnalyzing = true
                        validationMsg = "Capturing water surface pixels..."

                        val currentBitmap: Bitmap? = previewViewRef?.bitmap

                        if (currentBitmap == null) {
                            isAnalyzing = false
                            validationMsg = "⚠️ No image captured — align camera with water surface"
                            return@Button
                        }

                        coroutineScope.launch {
                            val result = withContext(Dispatchers.Default) {
                                MlKitWaterValidator.validateImage(currentBitmap)
                            }
                            isAnalyzing = false
                            validationMsg = result.feedbackMessage
                            onPhotoCaptured(result.feedbackMessage)
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = BioCyanPrimary),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Capture & Analyze Photo",
                        tint = OceanDarkBackground,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Front/Back Camera Switch Button
                IconButton(
                    onClick = {
                        isFrontCamera = !isFrontCamera
                        validationMsg = if (isFrontCamera) "Front camera mode active" else "Rear camera mode active"
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(OceanCardSurface, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Flip Camera",
                        tint = if (isFrontCamera) BioCyanPrimary else TextPrimaryWhite
                    )
                }
            }
        }
    }
}

