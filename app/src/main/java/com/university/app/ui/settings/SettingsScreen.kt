package com.university.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.university.app.AppService
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.AppTopBar
import com.university.app.ui.components.AvatarCircle
import com.university.app.ui.components.ConfirmDialog
import com.university.app.ui.theme.AbsentRed
import com.university.app.ui.theme.Navy
import com.university.app.ui.theme.SurfaceWhite
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

    Scaffold(
        topBar = { AppTopBar(title = "الإعدادات", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarCircle(initial = session?.name ?: "", size = 56)
                        Spacer(Modifier.size(16.dp))
                        Column {
                            Text(
                                session?.name ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "رقم القيد: ${session?.userId ?: "-"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = AbsentRed
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AbsentRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("تسجيل الخروج", fontWeight = FontWeight.SemiBold)
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "النظام الأكاديمي",
                    style = MaterialTheme.typography.titleSmall,
                    color = Navy,
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
                    text = "الإصدار 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
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
                Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = Navy)
                Spacer(Modifier.size(8.dp))
                Text("لوحة تحكم المشرف", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text("أدخل كلمة سر المشرف للوصول إلى لوحة التحكم", color = TextSecondary)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة سر المشرف") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { visible = !visible }) {
                            Icon(
                                imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
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
                Text("دخول", color = Navy, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء", color = TextSecondary) }
        },
        shape = MaterialTheme.shapes.large,
        containerColor = SurfaceWhite
    )
}