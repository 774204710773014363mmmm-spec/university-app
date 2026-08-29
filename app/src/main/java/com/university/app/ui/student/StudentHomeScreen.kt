package com.university.app.ui.student

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.university.app.AppService
import com.university.app.data.model.User
import com.university.app.ui.components.AvatarCircle
import com.university.app.ui.components.GlassCard
import com.university.app.ui.components.GlowCard
import com.university.app.ui.components.GradientBackground
import com.university.app.ui.theme.BackgroundDark
import com.university.app.ui.theme.DarkPurple
import com.university.app.ui.theme.GlassBorder
import com.university.app.ui.theme.GlowBlue
import com.university.app.ui.theme.GlowGreen
import com.university.app.ui.theme.GlowPurple
import com.university.app.ui.theme.NavBarBackground
import com.university.app.ui.theme.NavItemSelected
import com.university.app.ui.theme.NavItemUnselected
import com.university.app.ui.theme.RoyalBlue
import com.university.app.ui.theme.RoyalBlueDark
import com.university.app.ui.theme.SurfaceGlassLight
import com.university.app.ui.theme.TextSecondary
import com.university.app.util.SessionManager

private data class TabItem(val label: String, val icon: ImageVector, val glowColor: Color)

private val tabs = listOf(
    TabItem("الجدول اليومي", Icons.Filled.CalendarMonth, GlowBlue),
    TabItem("درجاتي", Icons.Filled.School, GlowPurple),
    TabItem("الحضور والغياب", Icons.Filled.FactCheck, GlowGreen)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentHomeScreen(onLogout: () -> Unit, onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val session = remember { SessionManager.current(context) }
    var user by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(session?.userId) {
        user = session?.userId?.let { id ->
            try {
                AppService.repo.getUser(id)
            } catch (e: Exception) {
                null
            }
        }
    }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    // Animation for background glow
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val glowX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowX"
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                text = "مرحباً، ${user?.name ?: session?.name ?: "طالب"}"
                                    .let { if (it.length > 24) it.take(24) + "…" else it },
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = user?.let { "المستوى ${it.currentLevel} - الترم ${it.currentTerm}" } ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = GlowBlue.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                navigationIcon = {
                    Spacer(Modifier.width(8.dp))
                    AvatarCircle(
                        initial = user?.name ?: session?.name ?: "",
                        size = 40,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "الإعدادات",
                            tint = GlowBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = NavBarBackground.copy(alpha = 0.95f),
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GlowBlue.copy(alpha = 0.3f),
                                Color.Transparent,
                                GlowPurple.copy(alpha = 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    val glowAlpha by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0f,
                        label = "navGlow"
                    )

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        tab.glowColor.copy(alpha = 0.4f),
                                                        Color.Transparent
                                                    )
                                                ),
                                                shape = CircleShape
                                            )
                                    )
                                }
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.label,
                                    tint = if (isSelected) tab.glowColor else NavItemUnselected,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        },
                        label = {
                            Text(
                                tab.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent,
                            selectedTextColor = GlowBlue,
                            unselectedTextColor = NavItemUnselected
                        )
                    )
                }
            }
        }
    ) { padding ->
        GradientBackground(
            modifier = Modifier.padding(padding)
        ) {
            // Animated glow orbs
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                RoyalBlue.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(glowX, 300f),
                            radius = 600f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                DarkPurple.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(1000f - glowX, 600f),
                            radius = 600f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Level Cards
                when (selectedTab) {
                    0 -> ScheduleTab(user = user)
                    1 -> GradesTab(user = user)
                    2 -> AttendanceTab(user = user)
                }
            }
        }
    }
}
