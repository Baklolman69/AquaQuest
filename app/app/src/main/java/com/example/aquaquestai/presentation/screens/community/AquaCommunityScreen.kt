package com.example.aquaquestai.presentation.screens.community

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aquaquestai.R
import com.example.aquaquestai.data.community.CommunityRepository
import com.example.aquaquestai.data.firebase.FirebaseRepository
import com.example.aquaquestai.domain.model.community.CommentObservationType
import com.example.aquaquestai.domain.model.community.CommunityProject
import com.example.aquaquestai.domain.model.community.CommunityQuest
import com.example.aquaquestai.domain.model.community.EcosystemObservation
import com.example.aquaquestai.domain.model.community.EventDiscussionComment
import com.example.aquaquestai.presentation.AquaQuestViewModel
import com.example.aquaquestai.theme.AlertCrimson
import com.example.aquaquestai.theme.EmeraldHealthy
import com.example.aquaquestai.theme.LightBackground
import com.example.aquaquestai.theme.LightBorder
import com.example.aquaquestai.theme.LightPrimary
import com.example.aquaquestai.theme.LightSurface
import com.example.aquaquestai.theme.LightTextMuted
import com.example.aquaquestai.theme.LightTextPrimary
import com.example.aquaquestai.theme.LightTextSecondary
import com.example.aquaquestai.theme.QuestPurple
import com.example.aquaquestai.theme.WarningAmber
import kotlinx.coroutines.launch

// ══════════════════════════════════════════════════════════════════════
// ██                    MAIN SCREEN COMPOSABLE                       ██
// ══════════════════════════════════════════════════════════════════════
@Composable
fun AquaCommunityScreen(
    viewModel: AquaQuestViewModel,
    onNavigateToScan: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    val discussions = remember { mutableStateListOf<EventDiscussionComment>() }
    val communityQuests = remember { mutableStateListOf<CommunityQuest>() }
    val projects = remember { mutableStateListOf<CommunityProject>() }
    val ecosystemLogs = remember { mutableStateListOf<EcosystemObservation>() }

    var newCommentText by remember { mutableStateOf("") }
    var selectedObservationType by remember { mutableStateOf(CommentObservationType.OBSERVED_DOWNSTREAM) }
    var showProposeQuestDialog by remember { mutableStateOf(false) }

    val username = remember { FirebaseRepository.getUserName(context) }

    LaunchedEffect(Unit) {
        isLoading = true
        discussions.clear()
        discussions.addAll(CommunityRepository.fetchDiscussions())
        communityQuests.clear()
        communityQuests.addAll(CommunityRepository.fetchQuests())
        projects.clear()
        projects.addAll(CommunityRepository.fetchProjects())
        ecosystemLogs.clear()
        ecosystemLogs.addAll(CommunityRepository.fetchEcosystemLogs())
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        // ── 1. Premium Banner Header ──
        CommunityBannerHeader()

        // ── 2. Tab Row ──
        CommunityTabRow(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it },
            discussionCount = discussions.size,
            questCount = communityQuests.size,
            projectCount = projects.size,
            ecoLogCount = ecosystemLogs.size,
        )

        // ── 3. Content Area ──
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (isLoading) {
                LoadingState()
            } else {
                when (selectedTab) {
                    0 -> DiscussionsTab(
                        discussions = discussions,
                        newCommentText = newCommentText,
                        onCommentTextChange = { newCommentText = it },
                        selectedObservationType = selectedObservationType,
                        onTypeSelect = { selectedObservationType = it },
                        onPostComment = {
                            if (newCommentText.isNotBlank()) {
                                val comment = EventDiscussionComment(
                                    id = "comm-${System.currentTimeMillis()}",
                                    eventId = "event-austin-01",
                                    authorName = username,
                                    authorRank = "Stream Guardian",
                                    content = newCommentText.trim(),
                                    observationType = selectedObservationType
                                )
                                discussions.add(0, comment)
                                newCommentText = ""
                                scope.launch { CommunityRepository.postComment(context, comment) }
                            }
                        }
                    )
                    1 -> QuestsTab(
                        quests = communityQuests,
                        onProposeQuestClick = { showProposeQuestDialog = true },
                        onJoinQuest = { quest ->
                            val idx = communityQuests.indexOfFirst { it.id == quest.id }
                            if (idx >= 0) {
                                communityQuests[idx] = communityQuests[idx].copy(
                                    participantCount = communityQuests[idx].participantCount + 1,
                                    isJoined = true
                                )
                                onNavigateToScan()
                            }
                        }
                    )
                    2 -> ProjectsTab(
                        projects = projects,
                        onToggleRsvp = { proj ->
                            val idx = projects.indexOfFirst { it.id == proj.id }
                            if (idx >= 0) {
                                val cur = projects[idx]
                                val newJoined = !cur.isJoined
                                projects[idx] = cur.copy(
                                    participantCount = cur.participantCount + if (newJoined) 1 else -1,
                                    isJoined = newJoined
                                )
                                scope.launch { CommunityRepository.toggleProjectRsvp(context, proj.id, newJoined) }
                            }
                        }
                    )
                    3 -> EcoLogsTab(logs = ecosystemLogs)
                }
            }
        }
    }

    if (showProposeQuestDialog) {
        ProposeQuestDialog(
            onDismiss = { showProposeQuestDialog = false },
            onSubmitQuest = { title, reason, location ->
                val newQuest = CommunityQuest(
                    id = "cq-${System.currentTimeMillis()}",
                    title = title,
                    reason = reason,
                    locationName = location,
                    latitude = 30.2672,
                    longitude = -97.7431,
                    creatorName = username
                )
                communityQuests.add(0, newQuest)
                showProposeQuestDialog = false
                scope.launch { CommunityRepository.proposeQuest(context, newQuest) }
            }
        )
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    BANNER HEADER                                ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun CommunityBannerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
    ) {
        // Background Image
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.aqua_community_header),
            contentDescription = "Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.88f),
                            Color.White.copy(alpha = 0.5f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 14.dp, top = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9))
                        )
                    )
                    .border(2.dp, Color.White.copy(alpha = 0.7f), CircleShape)
            ) {
                Text("🌍", fontSize = 22.sp)
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Aqua", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0284C7))
                    Text("Community", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                    Spacer(Modifier.width(6.dp))
                    LiveBadge()
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Connect ground evidence, propose quests &\njoin environmental action",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }

            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8))))
                    .border(2.dp, Color.White, CircleShape)
            ) {
                Text("👤", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun LiveBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.9f))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(EmeraldHealthy)
            )
            Spacer(Modifier.width(4.dp))
            Text("LIVE FIREBASE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = LightPrimary)
        }
    }
}

@Composable
private fun LoadingState() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = LightPrimary)
            Spacer(Modifier.height(8.dp))
            Text("Fetching live Firebase community data...", fontSize = 12.sp, color = LightTextSecondary)
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    TAB ROW                                      ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun CommunityTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    discussionCount: Int,
    questCount: Int,
    projectCount: Int,
    ecoLogCount: Int,
) {
    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = LightSurface,
        contentColor = LightPrimary,
        edgePadding = 12.dp,
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = LightPrimary,
                    height = 3.dp
                )
            }
        },
        modifier = Modifier.fillMaxWidth().shadow(2.dp)
    ) {
        CommunityTab(selectedTab == 0, { onTabSelected(0) }, "💬", "Discussions ($discussionCount)")
        CommunityTab(selectedTab == 1, { onTabSelected(1) }, "🎯", "Quests ($questCount)")
        CommunityTab(selectedTab == 2, { onTabSelected(2) }, "🌱", "Projects ($projectCount)")
        CommunityTab(selectedTab == 3, { onTabSelected(3) }, "🦋", "Eco Logs ($ecoLogCount)")
    }
}

@Composable
private fun CommunityTab(isSelected: Boolean, onClick: () -> Unit, icon: String, label: String) {
    Tab(
        selected = isSelected,
        onClick = onClick,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Spacer(Modifier.width(4.dp))
                Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    )
}


// ══════════════════════════════════════════════════════════════════════
// ██                    DISCUSSIONS TAB                               ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun DiscussionsTab(
    discussions: List<EventDiscussionComment>,
    newCommentText: String,
    onCommentTextChange: (String) -> Unit,
    selectedObservationType: CommentObservationType,
    onTypeSelect: (CommentObservationType) -> Unit,
    onPostComment: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Info Banner
        DiscussionInfoBanner()

        Spacer(Modifier.height(8.dp))

        // Discussion Cards
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            discussions.forEach { comm ->
                DiscussionCard(comment = comm)
            }
        }

        // Bottom: Chip bar + Input
        DiscussionInputBar(
            selectedType = selectedObservationType,
            onTypeSelect = onTypeSelect,
            commentText = newCommentText,
            onCommentTextChange = onCommentTextChange,
            onPostComment = onPostComment,
        )
    }
}

@Composable
private fun DiscussionInfoBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFECFDF5))
            .border(1.dp, EmeraldHealthy.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(EmeraldHealthy.copy(alpha = 0.15f))
            ) {
                Text("🌿", fontSize = 14.sp)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Citizen discussions provide ground observer context. Verified facts are designated by USGS telemetry signals.",
                fontSize = 11.sp,
                color = Color(0xFF475569),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(4.dp))
            Text("ⓘ", fontSize = 16.sp, color = LightPrimary)
        }
    }
}

@Composable
private fun DiscussionCard(comment: EventDiscussionComment) {
    val timeAgo = remember(comment.timestamp) {
        val diff = (System.currentTimeMillis() - comment.timestamp) / 60000
        when {
            diff < 60 -> "${diff}m ago"
            diff < 1440 -> "${diff / 60}h ago"
            else -> "${diff / 1440}d ago"
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Avatar + Name + Badge + Type tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF0EA5E9))
                            )
                        )
                ) {
                    Text(comment.observationType.icon, fontSize = 16.sp)
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.authorName,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = LightPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "${comment.authorRank} Level ${(comment.authorName.length % 5) + 1}",
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Observation type tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(observationTagColor(comment.observationType))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = comment.observationType.label,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // Verified + Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(EmeraldHealthy.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("✓ Verified", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = EmeraldHealthy)
                }
                Spacer(Modifier.width(8.dp))
                Text(timeAgo, fontSize = 10.sp, color = Color(0xFF94A3B8))
                Spacer(Modifier.width(4.dp))
                Text("⋮", fontSize = 14.sp, color = Color(0xFFCBD5E1))
            }

            Spacer(Modifier.height(8.dp))

            // Content text
            Text(
                text = comment.content,
                fontSize = 13.sp,
                color = Color(0xFF334155),
                lineHeight = 18.sp,
            )

            Spacer(Modifier.height(10.dp))

            // Fake image gallery row
            ImageGalleryRow()

            Spacer(Modifier.height(10.dp))

            // Like / Comment / Share row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Like
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👍", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("${comment.likesCount + 8}", fontSize = 12.sp, color = Color(0xFF64748B))
                }

                Spacer(Modifier.width(16.dp))

                // Comments
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💬", fontSize = 14.sp)
                    Spacer(Modifier.width(4.dp))
                    Text("${(comment.authorName.length % 4) + 1}", fontSize = 12.sp, color = Color(0xFF64748B))
                }

                Spacer(Modifier.weight(1f))

                // Share
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { }
                ) {
                    Text("↗", fontSize = 14.sp, color = Color(0xFF94A3B8))
                    Spacer(Modifier.width(4.dp))
                    Text("Share", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            }
        }
    }
}

@Composable
private fun ImageGalleryRow() {
    val galleryColors = listOf(
        listOf(Color(0xFF93C5FD), Color(0xFF3B82F6)),
        listOf(Color(0xFF6EE7B7), Color(0xFF10B981)),
        listOf(Color(0xFFFCD34D), Color(0xFFF59E0B)),
    )

    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        galleryColors.forEachIndexed { idx, colors ->
            Box(
                modifier = Modifier
                    .size(72.dp, 56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(colors)),
                contentAlignment = Alignment.Center
            ) {
                val icons = listOf("🏞️", "🌊", "🌿")
                Text(icons[idx], fontSize = 22.sp)
            }
        }

        // "+N" overlay thumbnail
        Box(
            modifier = Modifier
                .size(72.dp, 56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF1E293B).copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "+2",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

private fun observationTagColor(type: CommentObservationType): Color {
    return when (type) {
        CommentObservationType.OBSERVED_DOWNSTREAM -> Color(0xFF0284C7)
        CommentObservationType.UNABLE_TO_SEE -> Color(0xFF64748B)
        CommentObservationType.LOCAL_CONTEXT_CONSTRUCTION -> Color(0xFFF59E0B)
        CommentObservationType.GENERAL -> Color(0xFF8B5CF6)
    }
}

@Composable
private fun DiscussionInputBar(
    selectedType: CommentObservationType,
    onTypeSelect: (CommentObservationType) -> Unit,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onPostComment: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
        // Observation type chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CommentObservationType.values().forEach { type ->
                ObservationChip(
                    label = "${type.icon} ${type.label}",
                    isSelected = selectedType == type,
                    color = observationTagColor(type),
                    onClick = { onTypeSelect(type) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Input row
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
            ) {
                Text("✦", fontSize = 14.sp, color = LightPrimary)
            }

            Spacer(Modifier.width(8.dp))

            OutlinedTextField(
                value = commentText,
                onValueChange = onCommentTextChange,
                placeholder = { Text("Add ground observation comment...", fontSize = 12.sp, color = LightTextMuted) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
            )

            Spacer(Modifier.width(8.dp))

            IconButton(
                onClick = onPostComment,
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(listOf(Color(0xFF38BDF8), Color(0xFF0284C7)))
                    )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ObservationChip(label: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) color else Color(0xFFF1F5F9))
            .border(1.dp, if (isSelected) color else Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) Color.White else Color(0xFF475569),
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    QUESTS TAB                                    ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun QuestsTab(
    quests: List<CommunityQuest>,
    onProposeQuestClick: () -> Unit,
    onJoinQuest: (CommunityQuest) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onProposeQuestClick,
            colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Propose Community Verification Quest", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            quests.forEach { q -> QuestCard(quest = q, onJoinQuest = onJoinQuest) }
        }
    }
}

@Composable
private fun QuestCard(quest: CommunityQuest, onJoinQuest: (CommunityQuest) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📍", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(quest.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary, modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFECFDF5))
                        .border(1.dp, EmeraldHealthy, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("+${quest.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldHealthy)
                }
            }
            Text("Reason: ${quest.reason}", fontSize = 12.sp, color = LightTextSecondary, modifier = Modifier.padding(vertical = 6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Location: ${quest.locationName}", fontSize = 11.sp, color = LightTextMuted)
                Spacer(Modifier.weight(1f))
                Text("${quest.participantCount} Guardians", fontSize = 11.sp, color = LightPrimary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = { onJoinQuest(quest) },
                colors = ButtonDefaults.buttonColors(containerColor = if (quest.isJoined) EmeraldHealthy else QuestPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (quest.isJoined) "Joined ✓ Launch AI Verification Scan" else "Join & Start Verification Quest", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    PROJECTS TAB                                  ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ProjectsTab(
    projects: List<CommunityProject>,
    onToggleRsvp: (CommunityProject) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        projects.forEach { proj -> ProjectCard(project = proj, onToggleRsvp = onToggleRsvp) }
    }
}

@Composable
private fun ProjectCard(project: CommunityProject, onToggleRsvp: (CommunityProject) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(project.category.icon, fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(project.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary)
                    Text("${project.category.label} • ${project.scheduledDateStr}", fontSize = 11.sp, color = LightPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Text(project.description, fontSize = 12.sp, color = LightTextSecondary, modifier = Modifier.padding(vertical = 6.dp))
            Text("📊 Impact: ${project.impactSummary}", fontSize = 11.sp, color = LightTextMuted, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("👥 ${project.participantCount}/${project.maxParticipants} Guardians", fontSize = 11.sp, color = LightTextSecondary)
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onToggleRsvp(project) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (project.isJoined) EmeraldHealthy else LightPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (project.isJoined) "RSVP Joined ✓" else "Join Project", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    ECO LOGS TAB                                  ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun EcoLogsTab(logs: List<EcosystemObservation>) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        logs.forEach { log -> EcoLogCard(log = log) }
    }
}

@Composable
private fun EcoLogCard(log: EcosystemObservation) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(LightPrimary.copy(alpha = 0.12f))
            ) {
                Text("🦋", fontSize = 18.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.speciesName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary)
                Text("${log.category} • ${log.locationName}", fontSize = 11.sp, color = LightPrimary, fontWeight = FontWeight.Bold)
                if (log.notes.isNotEmpty()) {
                    Text(log.notes, fontSize = 11.sp, color = LightTextSecondary, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }
    }
}


// ══════════════════════════════════════════════════════════════════════
// ██                    PROPOSE QUEST DIALOG                          ██
// ══════════════════════════════════════════════════════════════════════
@Composable
private fun ProposeQuestDialog(
    onDismiss: () -> Unit,
    onSubmitQuest: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)).clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.fillMaxWidth(0.9f).clickable(enabled = false) {}.padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📍 Propose Community Quest", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = LightTextPrimary, modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = LightTextPrimary)
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Quest Title", color = LightTextMuted) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason", color = LightTextMuted) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location", color = LightTextMuted) }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { if (title.isNotBlank() && location.isNotBlank()) onSubmitQuest(title.trim(), reason.trim(), location.trim()) },
                    colors = ButtonDefaults.buttonColors(containerColor = LightPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Submit Proposed Quest", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
