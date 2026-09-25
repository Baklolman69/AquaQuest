package com.example.aquaquestai.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.data.firebase.FirebaseRepository
import com.example.aquaquestai.theme.*
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onOnboardingCompleted: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentPage by remember { mutableIntStateOf(0) }
    var usernameInput by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val slides = listOf(
        OnboardingSlide(
            icon = Icons.Default.Water,
            title = "Real-Time Telemetry & OSM Maps",
            description = "Access live USGS Water Data API sensors and vector river channels across 5 European pilot cities.",
            badge = "USGS NWIS OGC API"
        ),
        OnboardingSlide(
            icon = Icons.Default.CameraAlt,
            title = "Dual AI Vision Inspection",
            description = "Scan streams using on-device ML Kit pre-validation + Gemini 1.5 Vision WFD ecological diagnostics.",
            badge = "ML Kit + Gemini Vision"
        ),
        OnboardingSlide(
            icon = Icons.Default.HealthAndSafety,
            title = "AquaHealth Exposure Engine",
            description = "Connect symptoms, water exposure, and trusted CDC/WHO evidence for One Health intelligence.",
            badge = "One Health Standard"
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AQUAQUEST AI",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp
                    ),
                    color = LightPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (index == currentPage) 20.dp else 6.dp)
                                .clip(CircleShape)
                                .background(if (index == currentPage) LightPrimary else LightBorder)
                        )
                    }
                }
            }

            // Current Carousel Slide Content
            val currentSlide = slides[currentPage]
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                listOf(LightPrimary.copy(alpha = 0.2f), LightPrimary.copy(alpha = 0.05f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = currentSlide.icon,
                        contentDescription = currentSlide.title,
                        tint = LightPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = LightPrimary.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = currentSlide.badge,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = LightPrimary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = currentSlide.title,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = LightTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentSlide.description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = LightTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // User Name Input Card (Required on Last Slide or Bottom Card)
            Surface(
                color = LightSurface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, LightCardBorder, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Name",
                            tint = LightPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stream Guardian Name (Saved to Firebase)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = LightTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        placeholder = {
                            Text(
                                "e.g. Alex_HydroGuardian",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightTextMuted
                            )
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = LightPrimary,
                            unfocusedBorderColor = LightBorder,
                            focusedTextColor = LightTextPrimary,
                            unfocusedTextColor = LightTextPrimary
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (usernameInput.isNotBlank()) {
                                isSaving = true
                                scope.launch {
                                    FirebaseRepository.saveUserProfile(context, usernameInput)
                                    onOnboardingCompleted(usernameInput.trim())
                                }
                            }
                        }),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (currentPage < 2) {
                                currentPage++
                            } else {
                                val nameToSave = usernameInput.ifBlank { "StreamGuardian_${(1000..9999).random()}" }
                                isSaving = true
                                scope.launch {
                                    FirebaseRepository.saveUserProfile(context, nameToSave)
                                    onOnboardingCompleted(nameToSave.trim())
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentPage < 2) "Next Feature" else "Join the Quest & Start",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = if (currentPage < 2) Icons.AutoMirrored.Filled.ArrowForward else Icons.Default.Check,
                                    contentDescription = "Proceed",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class OnboardingSlide(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val title: String,
    val description: String,
    val badge: String
)
