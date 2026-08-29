package com.university.app.ui.settings

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.university.app.AppService
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.AppTopBar
import com.university.app.ui.components.AvatarCircle
import com.university.app.ui.components.ConfirmDialog
import com.university.app.ui.components.GlassCard
import com.university.app.ui.components.GlowCard
import com.university.app.ui.theme.AbsentRed
import com.university.app.ui.theme.BackgroundDark
import com.university.app.ui.theme.DarkPurple
import com.university.app.ui.theme.GlassBorder
import com.university.app.ui.theme.GlowBlue
import com.university.app.ui.theme.GlowGreen
import com.university.app.ui.theme.GlowOrange
import com.university.app.ui.theme.GlowPurple
import com.university.app.ui.theme.RoyalBlue
import com.university.app.ui.theme.TextSecondary
import com.university.app.util.SessionManager
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onAdminOpened: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val session = remember { SessionManager.current(context) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAdminDialog by remember { mutableStateOf(false) }
    var secretTaps by remember { mutableIntStateOf(0) }

    // Animated background glow
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
        topBar = { AppTopBar(title = "الإعدادات", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A),
                            Color(0xFF1B0A3C),
                            Color(0xFF0D1B2A)
                        )
                    )
                )
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

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Card
                item {
                    GlowCard(glowColor = GlowBlue) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(initial = session?.name ?: "", size = 56)
                            Spacer(Modifier.size(16.dp))
                            Column {
                                Text(
                                    session?.name ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "رقم القيد: ${session?.userId ?: "-"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GlowBlue.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // Settings Items
                item {
                    SettingsItem(
                        icon = Icons.Filled.Language,
                        title = "اللغة",
                        subtitle = "العربية",
                        glowColor = GlowBlue,
                        onClick = { }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Filled.FormatSize,
                        title = "حجم الخط",
                        subtitle = "متوسط",
                        glowColor = GlowPurple,
                        onClick = { }
                    )
                }

                item {
                    SettingsItem(
                        icon = Icons.Filled.Security,
                        title = "إعدادات الحساب",
                        subtitle = "إدارة كلمة المرور والبيانات",
                        glowColor = GlowGreen,
                        onClick = { }
                    )
                }

                // Logout Button
                item {
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = AbsentRed.copy(alpha = 0.3f),
                                spotColor = AbsentRed.copy(alpha = 0.5f)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = AbsentRed
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AbsentRed.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("تسجيل الخروج", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }

                // Academic System (Secret taps for admin)
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "النظام الأكاديمي",
                        style = MaterialTheme.typography.titleSmall,
                        color = GlowBlue.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                secretTaps++
                                if (secretTaps >= 5) {
                                    secretTaps = 0
                                    showAdminDialog = true
                                }
                            }
                            .padding(vertical = 16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "الإصدار 2.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (showLogoutDialog) {
        ConfirmDialog(
            title = "تسجيل الخروج",
            message = "هل تريد تسجيل الخروج من التطبيق؟",
            confirmText = "خروج",
            onConfirm = {
                showLogoutDialog = false
                SessionManager.clear(context)
                onLogout()
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showAdminDialog) {
        AdminPasswordDialog(
            onDismiss = { showAdminDialog = false },
            onSuccess = {
                showAdminDialog = false
                onAdminOpened()
            },
            onError = {
                scope.launch { snackbarHostState.showSnackbar("كلمة سر المشرف غير صحيحة") }
            }
        )
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    glowColor: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = glowColor.copy(alpha = 0.4f),
                        spotColor = glowColor.copy(alpha = 0.6f)
                    )
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(glowColor.copy(alpha = 0.3f), Color.Transparent)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = glowColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun AdminPasswordDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    onError: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    var checking by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = GlowPurple.copy(alpha = 0.5f),
                            spotColor = GlowPurple.copy(alpha = 0.7f)
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(GlowPurple.copy(alpha = 0.3f), Color.Transparent)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.AdminPanelSettings,
                        contentDescription = null,
                        tint = GlowPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.size(12.dp))
                Text(
                    "لوحة تحكم المشرف",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    "أدخل كلمة سر المشرف للوصول إلى لوحة التحكم",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة سر المشرف") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = GlowPurple)
                    },
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                tint = GlowPurple
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlowPurple,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = Color(0x15FFFFFF),
                        unfocusedContainerColor = Color(0x08FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = GlowPurple,
                        focusedLabelColor = GlowPurple,
                        unfocusedLabelColor = TextSecondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = password.isNotBlank() && !checking,
                onClick = {
                    scope.launch {
                        checking = true
                        val ok = try {
                            AppService.repo.checkAdminPassword(password)
                        } catch (e: Exception) {
                            false
                        } finally {
                            checking = false
                        }
                        if (ok) onSuccess() else onError()
                    }
                }
            ) {
                Text("دخول", color = GlowPurple, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextSecondary)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFF1B2A4A),
        titleContentColor = Color.White,
        textContentColor = TextSecondary
    )
}
