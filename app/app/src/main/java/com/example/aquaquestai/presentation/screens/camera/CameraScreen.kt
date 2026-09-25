package com.example.aquaquestai.presentation.screens.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import java.util.concurrent.Executors
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.aquaquestai.data.ai.GroqWaterAnalyzer
import com.example.aquaquestai.presentation.AquaQuestViewModel
import com.example.aquaquestai.presentation.WaterSamplePreset
import com.example.aquaquestai.presentation.components.GlassCard
import com.example.aquaquestai.presentation.components.RiskBar
import com.example.aquaquestai.theme.AlertCrimson
import com.example.aquaquestai.theme.EmeraldHealthy
import com.example.aquaquestai.theme.WarningAmber
import com.example.aquaquestai.theme.XpGradientEnd
import com.example.aquaquestai.theme.XpGradientStart
import kotlinx.coroutines.delay

enum class ScanState { IDLE, SCANNING, RESULT }

@Composable
fun CameraScreen(
    viewModel: AquaQuestViewModel,
    onNavigateToAquaHealth: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var scanState by remember { mutableStateOf(ScanState.IDLE) }
    var scanStepIndex by remember { mutableIntStateOf(0) }
    var submitted by remember { mutableStateOf(false) }
    var showAquaHealthPromptCard by remember { mutableStateOf(false) }

    LaunchedEffect(scanState) {
        if (scanState == ScanState.RESULT) {
            showAquaHealthPromptCard = false
            delay(2500) // 2.5 seconds after image scan result
            showAquaHealthPromptCard = true
        } else {
            showAquaHealthPromptCard = false
        }
    }


    val lifecycleOwner = LocalLifecycleOwner.current
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    // Selected Gallery Uri, Bitmap, Live CV Preset, Frame Backup, and Real Computed Water Metrics
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var galleryBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var livePreset by remember { mutableStateOf<WaterSamplePreset?>(null) }
    var latestFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }
    var realComputedPreset by remember { mutableStateOf<WaterSamplePreset?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            val decodedBitmap = loadNativeBitmapFromUri(context, uri)
            galleryBitmap = decodedBitmap

            if (decodedBitmap != null) {
                val localResult = WaterImageAnalyzer.analyzeBitmap(decodedBitmap)
                realComputedPreset = localResult
                coroutineScope.launch {
                    val groqResult = GroqWaterAnalyzer.analyzeWithGroq(decodedBitmap)
                    realComputedPreset = groqResult
                }
            }
            submitted = false
            scanState = ScanState.SCANNING
        }
    }

    val activeReportPreset = remember(scanState, realComputedPreset, viewModel.activePresetIndex) {
        realComputedPreset ?: viewModel.activePreset
    }

    val scanSteps = remember {
        listOf(
            "📷 Sampling pixel RGB channels & frame data...",
            "🤖 Querying qwen/qwen3.8-27b on Groq AI...",
            "⚡ Computing turbidity NTU, Forel-Ule & OneHealth risk...",
        )
    }

    LaunchedEffect(scanState) {
        if (scanState == ScanState.SCANNING) {
            scanStepIndex = 0
            delay(1000)
            scanStepIndex = 1
            delay(1000)
            scanStepIndex = 2
            delay(1200)
            scanState = ScanState.RESULT
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Control Bar
            TopCameraBar(
                onReset = {
                    scanState = ScanState.IDLE
                    selectedImageUri = null
                    galleryBitmap = null
                    realComputedPreset = null
                },
                onPickGallery = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                    )
                },
            )

            Spacer(Modifier.height(10.dp))

            when (scanState) {
                ScanState.IDLE -> {
                    // Viewfinder Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(20.dp),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (galleryBitmap != null) {
                            Image(
                                bitmap = galleryBitmap!!.asImageBitmap(),
                                contentDescription = "Gallery Water Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else if (hasCameraPermission) {
                            // Live CameraX Hardware Viewfinder Feed with Real-time Computer Vision Analyzer
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

                                            val imageAnalysis = ImageAnalysis.Builder()
                                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                                .build()

                                            val imageCapture = ImageCapture.Builder()
                                                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                                .build()
                                            imageCaptureRef = imageCapture

                                            val executor = Executors.newSingleThreadExecutor()
                                            var lastAnalysisTime = 0L

                                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                                val currentTime = System.currentTimeMillis()
                                                if (currentTime - lastAnalysisTime >= 250) { // 4 FPS live CV sampling
                                                    lastAnalysisTime = currentTime
                                                    try {
                                                        val bitmap = imageProxy.toBitmap()
                                                        if (bitmap != null) {
                                                            val computed = WaterImageAnalyzer.analyzeBitmap(bitmap)
                                                            ContextCompat.getMainExecutor(ctx).execute {
                                                                livePreset = computed
                                                                latestFrameBitmap = bitmap
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }
                                                }
                                                imageProxy.close()
                                            }

                                            val selector = CameraSelector.DEFAULT_BACK_CAMERA
                                            try {
                                                cameraProvider.unbindAll()
                                                cameraProvider.bindToLifecycle(
                                                    lifecycleOwner,
                                                    selector,
                                                    preview,
                                                    imageAnalysis,
                                                    imageCapture
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
                            // Tap / Button to Enable Camera Permission
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Enable Camera",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Camera Permission Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "AquaQuest uses live camera access to perform Groq AI spectrometry analysis on stream surface water.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Allow Camera Permission", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        CameraHudOverlay()

                        // Real-Time Live Computer Vision Bounding Boxes Overlay (Green / Red Boxes)
                        if (hasCameraPermission && galleryBitmap == null && livePreset != null) {
                            VisionBoundingBoxOverlay(boxes = livePreset!!.boundingBoxes)
                        }

                        // Real-Time Live Computer Vision Telemetry Badge
                        if (hasCameraPermission && galleryBitmap == null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(14.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.68f))
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 10.dp, vertical = 8.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF00FF66))
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "LIVE CV TELEMETRY",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "💧 Turbidity: ${livePreset?.turbidityNtu ?: "Scanning..."}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        "✨ Clarity: ${livePreset?.clarityScore ?: "--"}/10  |  pH: ${livePreset?.ph ?: "7.2"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        "🌈 Sheen Risk: ${((livePreset?.oilSheenRisk ?: 0.05f) * 100).toInt()}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 14.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldHealthy),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (galleryBitmap != null) "🖼️ Custom Gallery Photo Selected"
                                    else "💧 Stream Surface Aligned (Vision Ready)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Scenario Presets Selector Bar
                    Text(
                        "🧪 Test AI Scenario Presets",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        viewModel.waterPresets.forEachIndexed { index, preset ->
                            val isSelected = viewModel.activePresetIndex == index && galleryBitmap == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    )
                                    .clickable {
                                        galleryBitmap = null
                                        selectedImageUri = null
                                        realComputedPreset = null
                                        viewModel.activePresetIndex = index
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                            ) {
                                Text(
                                    "${preset.icon} ${preset.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }

                    Spacer(Modifier.weight(1f))

                    // Bottom Shutter Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp, start = 32.dp, end = 32.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                                    )
                                }
                                .padding(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Filled.PhotoLibrary,
                                    "Gallery",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Gallery",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .border(
                                        width = 4.dp,
                                        brush = Brush.sweepGradient(
                                            listOf(XpGradientStart, XpGradientEnd, XpGradientStart),
                                        ),
                                        shape = CircleShape,
                                    )
                                    .clickable {
                                        if (!hasCameraPermission && galleryBitmap == null) {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        } else {
                                            submitted = false
                                            scanState = ScanState.SCANNING

                                            val targetBitmap = galleryBitmap ?: latestFrameBitmap ?: previewViewRef?.bitmap
                                            if (targetBitmap != null) {
                                                val localResult = WaterImageAnalyzer.analyzeBitmap(targetBitmap)
                                                realComputedPreset = localResult
                                                coroutineScope.launch {
                                                    val groqResult = GroqWaterAnalyzer.analyzeWithGroq(targetBitmap)
                                                    realComputedPreset = groqResult
                                                }
                                            } else if (livePreset != null) {
                                                realComputedPreset = livePreset
                                            }

                                            // Trigger hardware ImageCapture frame grabber if available
                                            if (galleryBitmap == null && imageCaptureRef != null) {
                                                try {
                                                    imageCaptureRef!!.takePicture(
                                                        ContextCompat.getMainExecutor(context),
                                                        object : ImageCapture.OnImageCapturedCallback() {
                                                            override fun onCaptureSuccess(image: ImageProxy) {
                                                                val capBitmap = image.toBitmap()
                                                                image.close()
                                                                if (capBitmap != null) {
                                                                    val localResult = WaterImageAnalyzer.analyzeBitmap(capBitmap)
                                                                    realComputedPreset = localResult
                                                                    coroutineScope.launch {
                                                                        val groqResult = GroqWaterAnalyzer.analyzeWithGroq(capBitmap)
                                                                        realComputedPreset = groqResult
                                                                    }
                                                                }
                                                            }

                                                            override fun onError(exception: ImageCaptureException) {
                                                                exception.printStackTrace()
                                                            }
                                                        }
                                                    )
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Analyze Photo",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Spacer(Modifier.width(48.dp))
                    }
                }

                ScanState.SCANNING -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (galleryBitmap != null) {
                            Image(
                                bitmap = galleryBitmap!!.asImageBitmap(),
                                contentDescription = "Scanning Gallery Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                            )
                        }

                        AiLaserScanOverlay()

                        GlassCard(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            cornerRadius = 16.dp,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "On-Device Vision Analyzing...",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        scanSteps[scanStepIndex],
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }
                        }
                    }
                }

                ScanState.RESULT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        ScanResultReport(
                            preset = activeReportPreset,
                            capturedBitmap = galleryBitmap ?: latestFrameBitmap ?: previewViewRef?.bitmap,
                            isRealImageAnalysis = realComputedPreset != null,
                            submitted = submitted,
                            onSubmit = {
                                viewModel.addObservation(
                                    title = activeReportPreset.name,
                                    snippet = "Water Quality: ${activeReportPreset.clarityScore}/10 — Image Analyzed",
                                    type = activeReportPreset.statusType,
                                    ph = activeReportPreset.ph,
                                    turbidity = activeReportPreset.turbidityNtu,
                                    microplasticRisk = "${(activeReportPreset.microplasticRisk * 10).toInt()}/10",
                                )
                                submitted = true
                            },
                            onRetake = {
                                scanState = ScanState.IDLE
                                selectedImageUri = null
                                galleryBitmap = null
                                realComputedPreset = null
                            },
                        )

                        // ── 2.5s Post-Scan Contextual AquaHealth Card ──
                        AnimatedVisibility(
                            visible = showAquaHealthPromptCard,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut(),
                            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp)
                        ) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                cornerRadius = 16.dp
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                        ) {
                                            Text("🩺", fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "AquaHealth Exposure Check",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "Did you or anyone nearby experience symptoms after stream exposure?",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(onClick = { showAquaHealthPromptCard = false }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.primary)
                                            .clickable {
                                                showAquaHealthPromptCard = false
                                                onNavigateToAquaHealth()
                                            }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Check Exposure & Symptoms",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Composable
private fun TopCameraBar(
    onReset: () -> Unit,
    onPickGallery: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onReset) {
            Icon(Icons.Filled.Close, "Reset", tint = MaterialTheme.colorScheme.onBackground)
        }
        Text(
            "AI Stream Camera",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPickGallery) {
                Icon(
                    Icons.Filled.PhotoLibrary,
                    "Open Gallery",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = { }) {
                Icon(
                    Icons.Filled.HelpOutline,
                    "Field Guide",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CameraHudOverlay() {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cornerLength = 40.dp.toPx()
        val strokeWidth = 3.dp.toPx()

        val gridColor = primaryColor.copy(alpha = 0.15f)
        drawLine(gridColor, Offset(w / 3, 0f), Offset(w / 3, h), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(w * 2 / 3, 0f), Offset(w * 2 / 3, h), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, h / 3), Offset(w, h / 3), strokeWidth = 1.dp.toPx())
        drawLine(gridColor, Offset(0f, h * 2 / 3), Offset(w, h * 2 / 3), strokeWidth = 1.dp.toPx())

        // Corner Brackets
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(20.dp.toPx(), 20.dp.toPx() + cornerLength)
                lineTo(20.dp.toPx(), 20.dp.toPx())
                lineTo(20.dp.toPx() + cornerLength, 20.dp.toPx())
            },
            color = primaryColor,
            style = Stroke(strokeWidth),
        )
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w - 20.dp.toPx() - cornerLength, 20.dp.toPx())
                lineTo(w - 20.dp.toPx(), 20.dp.toPx())
                lineTo(w - 20.dp.toPx(), 20.dp.toPx() + cornerLength)
            },
            color = primaryColor,
            style = Stroke(strokeWidth),
        )
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(20.dp.toPx(), h - 20.dp.toPx() - cornerLength)
                lineTo(20.dp.toPx(), h - 20.dp.toPx())
                lineTo(20.dp.toPx() + cornerLength, h - 20.dp.toPx())
            },
            color = primaryColor,
            style = Stroke(strokeWidth),
        )
        drawPath(
            path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w - 20.dp.toPx() - cornerLength, h - 20.dp.toPx())
                lineTo(w - 20.dp.toPx(), h - 20.dp.toPx())
                lineTo(w - 20.dp.toPx(), h - 20.dp.toPx() - cornerLength)
            },
            color = primaryColor,
            style = Stroke(strokeWidth),
        )
    }
}

@Composable
private fun AiLaserScanOverlay() {
    val transition = rememberInfiniteTransition(label = "laser_sweep")
    val laserYRatio by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "laser_y",
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val currentLaserY = h * laserYRatio

        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    primaryColor,
                    Color.White,
                    primaryColor,
                    Color.Transparent,
                ),
            ),
            start = Offset(0f, currentLaserY),
            end = Offset(w, currentLaserY),
            strokeWidth = 4.dp.toPx(),
        )

        drawRect(
            color = primaryColor,
            topLeft = Offset(w * 0.15f, h * 0.25f),
            size = Size(w * 0.7f, h * 0.45f),
            style = Stroke(width = 2.dp.toPx()),
        )
    }
}

@Composable
private fun VisionBoundingBoxOverlay(
    boxes: List<com.example.aquaquestai.presentation.VisionBoundingBox>,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        boxes.forEach { box ->
            val boxColor = if (box.isAnomaly) Color(0xFFFF2A4B) else Color(0xFF00FF66)
            val fillColor = if (box.isAnomaly) Color(0x35FF2A4B) else Color(0x1800FF66)

            val leftPx = containerWidth * box.leftNorm
            val topPx = containerHeight * box.topNorm
            val rightPx = containerWidth * box.rightNorm
            val bottomPx = containerHeight * box.bottomNorm

            val boxWidth = (rightPx - leftPx).coerceAtLeast(24.dp)
            val boxHeight = (bottomPx - topPx).coerceAtLeast(24.dp)

            Box(
                modifier = Modifier
                    .offset(x = leftPx, y = topPx)
                    .size(width = boxWidth, height = boxHeight)
                    .background(fillColor, shape = RoundedCornerShape(8.dp))
                    .border(2.dp, boxColor, RoundedCornerShape(8.dp))
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = 4.dp, y = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(boxColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${if (box.isAnomaly) "🟥" else "🟩"} ${box.label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

private fun loadNativeBitmapFromUri(context: Context, uri: Uri): Bitmap? {
    return try {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun ScanResultReport(
    preset: WaterSamplePreset,
    capturedBitmap: Bitmap?,
    isRealImageAnalysis: Boolean,
    submitted: Boolean,
    onSubmit: () -> Unit,
    onRetake: () -> Unit,
) {
    val statusColor = when (preset.statusType) {
        com.example.aquaquestai.presentation.screens.map.MarkerType.HEALTHY -> EmeraldHealthy
        com.example.aquaquestai.presentation.screens.map.MarkerType.WARNING -> WarningAmber
        com.example.aquaquestai.presentation.screens.map.MarkerType.ALERT -> AlertCrimson
        com.example.aquaquestai.presentation.screens.map.MarkerType.QUEST -> MaterialTheme.colorScheme.primary
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            // Real Image Analysis Badge
            if (isRealImageAnalysis) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📊", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "REAL PHOTO ANALYSIS: Computed from 6,400 Spectrometry Data Points",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Captured Photo with Real AI Vision Green and Red Bounding Boxes Overlay
            if (capturedBitmap != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Image(
                        bitmap = capturedBitmap.asImageBitmap(),
                        contentDescription = "Analyzed Water Photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Real AI Vision Green & Red Spatial Bounding Boxes Overlaid on Captured Image!
                    VisionBoundingBoxOverlay(boxes = preset.boundingBoxes)
                }
                Spacer(Modifier.height(8.dp))

                val greenCount = preset.boundingBoxes.count { !it.isAnomaly }
                val redCount = preset.boundingBoxes.count { it.isAnomaly }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00FF66)))
                        Spacer(Modifier.width(6.dp))
                        Text("$greenCount Clean Water Zones (🟩)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFF2A4B)))
                        Spacer(Modifier.width(6.dp))
                        Text("$redCount Anomaly Regions (🟥)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (redCount > 0) AlertCrimson else MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    "AI Analysis",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "AI Water Health Diagnostics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Spacer(Modifier.height(12.dp))

            // Score Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(preset.icon, fontSize = 28.sp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${preset.name} • ${preset.clarityScore} / 10",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                    )
                    Text(
                        preset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // AI Computer Vision & Groq Reasoning Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                cornerRadius = 14.dp
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🤖", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "AI Vision & Spectrometry Reasoning",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = if (preset.aiReasoning.isNotBlank()) preset.aiReasoning
                        else "🧠 CameraX CV Spectrometry: Sampled 6,400 RGB pixel data points. Luminance variance yields clarity ${preset.clarityScore}/10 and turbidity ${preset.turbidityNtu}.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // OneHealth Triad Risk Card
            Text(
                "OneHealth Triad Health Rating",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TriadMetricItem(
                    icon = Icons.Filled.Sanitizer,
                    title = "Human Exposure",
                    value = if (preset.ph in 6.5..8.5) "Safe" else "Caution",
                    color = if (preset.ph in 6.5..8.5) EmeraldHealthy else WarningAmber,
                )
                TriadMetricItem(
                    icon = Icons.Filled.Pets,
                    title = "Pet Drinking",
                    value = if (preset.oilSheenRisk > 0.45f) "Unsafe" else "Moderate",
                    color = if (preset.oilSheenRisk > 0.45f) AlertCrimson else EmeraldHealthy,
                )
                TriadMetricItem(
                    icon = Icons.Filled.Water,
                    title = "Eco Health",
                    value = "${(preset.clarityScore * 10).toInt()}%",
                    color = statusColor,
                )
            }

            Spacer(Modifier.height(14.dp))

            // Parameter breakdowns
            Text(
                "Diagnostic Parameters",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(8.dp))

            RiskBar(label = "Turbidity (${preset.turbidityNtu})", progress = (preset.clarityScore / 10).toFloat(), color = statusColor)
            Spacer(Modifier.height(8.dp))
            RiskBar(label = "Oil Sheen Probability", progress = preset.oilSheenRisk, color = if (preset.oilSheenRisk > 0.4f) AlertCrimson else EmeraldHealthy)
            Spacer(Modifier.height(8.dp))
            RiskBar(label = "Microplastics Index", progress = preset.microplasticRisk, color = WarningAmber)

            Spacer(Modifier.height(12.dp))

            // AI Advisor Advice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .padding(10.dp),
            ) {
                Text(
                    "💡 AI Advisor: ${preset.advice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(Modifier.height(8.dp))

            // Citizen-Science Disclaimer Notice
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                    .padding(8.dp),
            ) {
                Text(
                    "ℹ️ Disclaimer: ${preset.disclaimer}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp
                )
            }

            Spacer(Modifier.height(16.dp))

            if (!submitted) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(XpGradientStart, XpGradientEnd),
                            ),
                        )
                        .clickable(onClick = onSubmit)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "📍 Post to OpenStreetMap & Claim +100 XP",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldHealthy)
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        "Done",
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Posted to OpenStreetMap! (+100 XP)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onRetake)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Refresh,
                    "Retake",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Scan Another Water Sample",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun TriadMetricItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    color: Color,
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, title, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 9.sp,
        )
        Text(
            value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}
