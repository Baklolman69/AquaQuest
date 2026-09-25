package com.example.aquaquestai.presentation.screens.aquahealth

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.data.AquaHealthAssessmentResult
import com.example.aquaquestai.data.AquaHealthExposureEngine
import com.example.aquaquestai.data.AquaHealthRelevance
import com.example.aquaquestai.data.UsgsWaterHealthReport
import com.example.aquaquestai.presentation.AquaQuestViewModel
import com.example.aquaquestai.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquaHealthScreen(
    viewModel: AquaQuestViewModel,
    activeWaterReport: UsgsWaterHealthReport? = null,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) } // 0: Intro, 1: Symptoms, 2: Exposure, 3: Result

    // Symptoms State
    val symptomOptions = listOf(
        "🚽 Diarrhea", "🤢 Nausea", "🤮 Vomiting", "🩺 Abdominal pain",
        "🌡️ Fever", "🧠 Headache", "🧴 Skin irritation", "🔴 Rash",
        "👁️ Eye irritation", "🗣️ Sore throat", "😮‍💨 Cough", "💤 Fatigue"
    )
    val selectedSymptoms = remember { mutableStateListOf<String>() }
    var customSymptom by remember { mutableStateOf("") }

    // Exposure State
    val exposureOptions = listOf(
        "🚰 Drank untreated surface water",
        "🛢️ Drank well water",
        "🥛 Drank tap water",
        "🏊 Swam in freshwater",
        "👣 Waded in freshwater",
        "🌊 Contact with floodwater",
        "🎣 Fishing / Recreational water exposure",
        "🚜 Worked near freshwater",
        "🚫 No known water exposure"
    )
    val selectedExposures = remember { mutableStateListOf<String>() }
    var selectedTiming by remember { mutableStateOf("1–2 days ago") }

    // Assessment Result State
    var assessmentResult by remember { mutableStateOf<AquaHealthAssessmentResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "🩺 AquaHealth",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = LightTextPrimary
                        )
                        Text(
                            text = "Water–Health Exposure Assessment",
                            style = MaterialTheme.typography.labelSmall,
                            color = LightTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    if (onBack != null || currentStep > 0) {
                        IconButton(onClick = {
                            if (currentStep > 0) currentStep-- else onBack?.invoke()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = LightPrimary)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = LightSurface)
            )
        },
        containerColor = LightBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual Step Progress Bar
            StepProgressBar(
                currentStep = currentStep,
                stepLabels = listOf("Overview", "Symptoms", "Exposure", "Analysis")
            )

            when (currentStep) {
                0 -> IntroStep(onStart = { currentStep = 1 })
                1 -> SymptomsStep(
                    options = symptomOptions,
                    selected = selectedSymptoms,
                    customSymptom = customSymptom,
                    onCustomChange = { customSymptom = it },
                    onNext = { currentStep = 2 }
                )
                2 -> ExposureStep(
                    options = exposureOptions,
                    selected = selectedExposures,
                    selectedTiming = selectedTiming,
                    onTimingChange = { selectedTiming = it },
                    onEvaluate = {
                        assessmentResult = AquaHealthExposureEngine.evaluateExposure(
                            symptoms = selectedSymptoms,
                            otherSymptomText = customSymptom,
                            exposures = selectedExposures,
                            timing = selectedTiming,
                            waterReport = activeWaterReport
                        )
                        currentStep = 3
                    }
                )
                3 -> assessmentResult?.let { result ->
                    ResultStep(
                        result = result,
                        activeWaterReport = activeWaterReport,
                        onReset = {
                            selectedSymptoms.clear()
                            selectedExposures.clear()
                            customSymptom = ""
                            currentStep = 0
                        },
                        onOpenUrl = { url ->
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IntroStep(onStart: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        shadowElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🩺", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Water–Health Exposure Assessment",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Could your recent water or environmental exposure be relevant to what you're experiencing?",
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onStart,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Start Assessment",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF0284C7)
                        )
                    }
                }
            }
        }
    }

    // Informational Positioning Card
    Surface(
        color = LightSurface,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightBorder, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📌 What AquaHealth Does",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• Connects symptoms + water exposure + timing + USGS telemetry + CDC/WHO health evidence.\n• Provides environmental exposure risk context, NOT a medical diagnosis.\n• Never invents fake probabilities or claims disease causation.",
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                color = LightTextSecondary
            )
        }
    }
}

@Composable
private fun SymptomsStep(
    options: List<String>,
    selected: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    customSymptom: String,
    onCustomChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Text(
        text = "1. Select Reported Symptoms",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = LightTextPrimary
    )

    // Urgent Warning Banner preview
    val hasSevere = selected.any { s ->
        s.contains("Fever", ignoreCase = true) || s.contains("Vomiting", ignoreCase = true) || s.contains("Diarrhea", ignoreCase = true)
    }

    if (hasSevere) {
        Surface(
            color = AlertCrimson.copy(alpha = 0.12f),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().border(1.dp, AlertCrimson.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Urgent Warning", tint = AlertCrimson)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "⚠️ Some symptoms reported may require prompt medical evaluation. Please consult a physician.",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = AlertCrimson
                )
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { item ->
            val isChecked = selected.contains(item)
            Surface(
                color = if (isChecked) LightPrimary.copy(alpha = 0.12f) else LightSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (isChecked) selected.remove(item) else selected.add(item)
                    }
                    .border(
                        1.dp,
                        if (isChecked) LightPrimary else LightBorder,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            if (isChecked) selected.remove(item) else selected.add(item)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = LightPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LightTextPrimary
                    )
                }
            }
        }
    }

    OutlinedTextField(
        value = customSymptom,
        onValueChange = onCustomChange,
        placeholder = { Text("Other symptom (optional)...", style = MaterialTheme.typography.bodySmall) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )

    Button(
        onClick = onNext,
        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Next: Water Exposure", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
private fun ExposureStep(
    options: List<String>,
    selected: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    selectedTiming: String,
    onTimingChange: (String) -> Unit,
    onEvaluate: () -> Unit
) {
    Text(
        text = "2. Recent Water Exposure & Timing",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = LightTextPrimary
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        options.forEach { item ->
            val isChecked = selected.contains(item)
            Surface(
                color = if (isChecked) LightPrimary.copy(alpha = 0.12f) else LightSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        if (isChecked) selected.remove(item) else selected.add(item)
                    }
                    .border(
                        1.dp,
                        if (isChecked) LightPrimary else LightBorder,
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = {
                            if (isChecked) selected.remove(item) else selected.add(item)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = LightPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = LightTextPrimary
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    Text(
        text = "Approximately when did exposure occur?",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = LightTextPrimary
    )

    val timingOptions = listOf("Today", "1–2 days ago", "3–7 days ago", "More than 7 days ago")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        timingOptions.forEach { t ->
            val isSelected = selectedTiming == t
            Surface(
                color = if (isSelected) LightPrimary else LightSurface,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTimingChange(t) }
                    .border(1.dp, if (isSelected) LightPrimary else LightBorder, RoundedCornerShape(12.dp))
            ) {
                Text(
                    text = t,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) Color.White else LightTextPrimary,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }

    Button(
        onClick = onEvaluate,
        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Evaluate Environmental Exposure", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
private fun ResultStep(
    result: AquaHealthAssessmentResult,
    activeWaterReport: UsgsWaterHealthReport?,
    onReset: () -> Unit,
    onOpenUrl: (String) -> Unit
) {
    val bannerColor = when (result.relevanceLevel) {
        AquaHealthRelevance.ELEVATED -> AlertCrimson
        AquaHealthRelevance.POSSIBLE -> WarningAmber
        AquaHealthRelevance.LOW -> EmeraldHealthy
    }

    // ── 1. Hero Assessment Banner ──
    Surface(
        color = bannerColor.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, bannerColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = bannerColor,
                    shape = CircleShape,
                    modifier = Modifier.size(10.dp)
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.relevanceTitle.replace("🔴 ", "").replace("🟡 ", "").replace("🟢 ", ""),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = bannerColor
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = result.relevanceSummary,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp, fontWeight = FontWeight.Medium),
                color = LightTextPrimary
            )
        }
    }

    // ── Urgent Warning (if severe symptoms detected) ──
    result.urgentWarningText?.let { warning ->
        Surface(
            color = AlertCrimson.copy(alpha = 0.12f),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AlertCrimson.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = "Medical Alert", tint = AlertCrimson)
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = warning, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = AlertCrimson)
            }
        }
    }

    // ── 2. Report Overview Grid Card ──
    Surface(
        color = LightSurface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📋 Assessment Overview",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OverviewGridRow(
                    icon = "🩺",
                    label = "Symptoms",
                    value = result.reportedSymptomsList.joinToString(", ")
                )
                OverviewGridRow(
                    icon = "🚰",
                    label = "Exposure & Timing",
                    value = "${result.reportedExposuresList.joinToString(", ")} (${result.exposureTimingLabel})"
                )
                OverviewGridRow(
                    icon = "🌊",
                    label = "Telemetry Station",
                    value = result.environmentalEvidenceText
                )
                OverviewGridRow(
                    icon = "📊",
                    label = "Evidence Confidence",
                    value = "${result.confidenceLabel} — ${result.confidenceExplanation}"
                )
            }
        }
    }

    // ── 3. Key Takeaways & Guidance ──
    Surface(
        color = LightBlueTint,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightCardBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Key Takeaways",
                    tint = LightPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Key Takeaways & Insights",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = LightPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TakeawayItem(
                    emoji = "💧",
                    title = "Water Exposure Note",
                    body = "Ingestion or contact with unmonitored water can introduce microbial pathogens. Symptoms typically emerge between 1 to 14 days."
                )
                TakeawayItem(
                    emoji = "🔬",
                    title = "Sensor Coverage",
                    body = "Automated sensors measure physical water properties (pH, clarity, oxygen) — not microscopic bacteria or viruses."
                )
                TakeawayItem(
                    emoji = "🩺",
                    title = "Recommended Action",
                    body = "Hydrate with clean, safe water. Consult a healthcare provider if symptoms persist, worsen, or cause concern."
                )
            }
        }
    }

    // ── 4. Public Health Sources & Disclaimer ──
    Surface(
        color = LightSurface,
        shape = RoundedCornerShape(20.dp),
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightBorder, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📚 Trusted Health Portals",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                result.healthSources.take(4).forEach { source ->
                    Surface(
                        color = LightPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenUrl(source.url) }
                            .border(1.dp, LightPrimary.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = source.name,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = LightPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.AutoMirrored.Filled.Launch,
                                contentDescription = "Open Source",
                                tint = LightPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "📌 Note: ${result.limitationsBanner}",
                style = MaterialTheme.typography.labelSmall.copy(lineHeight = 16.sp),
                color = LightTextMuted
            )
        }
    }

    Spacer(modifier = Modifier.height(4.dp))

    Button(
        onClick = onReset,
        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Start New Assessment", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
    }
}

@Composable
private fun OverviewGridRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = LightTextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = LightTextPrimary
            )
        }
    }
}

@Composable
private fun TakeawayItem(emoji: String, title: String, body: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = LightTextPrimary
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                color = LightTextSecondary
            )
        }
    }
}

@Composable
private fun StepProgressBar(
    currentStep: Int,
    stepLabels: List<String>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        stepLabels.forEachIndexed { index, label ->
            val isActive = index == currentStep
            val isPassed = index < currentStep

            val pillBg = when {
                isActive -> LightPrimary
                isPassed -> LightPrimary.copy(alpha = 0.25f)
                else -> LightSurfaceVariant
            }

            val contentColor = when {
                isActive -> Color.White
                isPassed -> LightPrimary
                else -> LightTextMuted
            }

            Surface(
                color = pillBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .border(
                        width = 1.dp,
                        color = if (isActive) LightPrimary else LightBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isPassed) "✓" else "${index + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = contentColor,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = contentColor,
                        fontSize = 9.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

