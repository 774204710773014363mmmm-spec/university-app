package com.university.app.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.university.app.AppService
import com.university.app.data.model.Role
import com.university.app.data.model.User
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.AppTopBar
import com.university.app.ui.components.AvatarCircle
import com.university.app.ui.components.ConfirmDialog
import com.university.app.ui.components.DropdownField
import com.university.app.ui.components.EmptyState
import com.university.app.ui.components.LEVEL_LABELS
import com.university.app.ui.components.LoadingBox
import com.university.app.ui.components.PrimaryButton
import com.university.app.ui.components.SectionTitle
import com.university.app.ui.components.TERM_LABELS
import com.university.app.ui.components.levelNumber
import com.university.app.ui.components.termNumber
import com.university.app.ui.theme.AbsentRed
import com.university.app.ui.theme.Navy
import com.university.app.ui.theme.SurfaceWhite
import com.university.app.ui.theme.TextSecondary
import com.university.app.util.ExcelReader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageStudentsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var showAddForm by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<User?>(null) }
    var importPreview by remember { mutableStateOf<List<User>?>(null) }
    var importing by remember { mutableStateOf(false) }

    var name by rememberSaveable { mutableStateOf("") }
    var number by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var level by rememberSaveable { mutableStateOf("المستوى الأول") }
    var term by rememberSaveable { mutableStateOf("الترم الأول") }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val rows = try {
                    ExcelReader.readUsers(context, uri)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("فشل قراءة الملف: ${e.message}")
                    emptyList()
                }
                if (rows.isNotEmpty()) importPreview = rows
                else snackbarHostState.showSnackbar("لم يتم العثور على بيانات صالحة في الملف")
            }
        }
    }

    fun reload() {
        scope.launch {
            loading = true
            students = try {
                AppService.repo.getStudents()
            } catch (e: Exception) {
                emptyList()
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    fun addStudent() {
        if (name.isBlank() || number.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("الاسم ورقم القيد مطلوبان") }
            return
        }
        scope.launch {
            val user = User(
                id = number.trim(),
                name = name.trim(),
                role = Role.STUDENT,
                phone = phone.trim(),
                currentLevel = levelNumber(level),
                currentTerm = termNumber(term),
                password = password.ifBlank { "123456" }
            )
            try {
                AppService.repo.addStudent(user)
                snackbarHostState.showSnackbar("تمت إضافة الطالب ${user.name} بنجاح")
                name = ""; number = ""; phone = ""; password = ""
                showAddForm = false
                reload()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("فشل الإضافة: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "إدارة حسابات الطلاب", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { showAddForm = !showAddForm },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text(if (showAddForm) "إغلاق النموذج" else "إضافة طالب", fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = {
                            excelLauncher.launch(
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/vnd.ms-excel",
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "text/plain"
                                )
                            )
                        },
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                    ) {
                        Icon(Icons.Filled.UploadFile, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("استيراد من إكسل", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (showAddForm) {
                item {
                    AppCard {
                        Column {
                            Text("بيانات الطالب الجديد", style = MaterialTheme.typography.titleMedium, color = Navy, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(16.dp))
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("اسم الطالب") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = number,
                                onValueChange = { number = it },
                                label = { Text("الرقم الجامعي") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("رقم الجوال (اختياري)") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("كلمة المرور (افتراضياً 123456)") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                DropdownField(
                                    label = "المستوى",
                                    options = LEVEL_LABELS,
                                    selected = level,
                                    onSelect = { level = it },
                                    modifier = Modifier.weight(1f)
                                )
                                DropdownField(
                                    label = "الترم",
                                    options = TERM_LABELS,
                                    selected = term,
                                    onSelect = { term = it },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton(text = "إضافة الطالب", onClick = ::addStudent, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            item { SectionTitle("الطلاب المسجلون (${students.size})") }

            when {
                loading -> item { LoadingBox() }
                students.isEmpty() -> item { EmptyState("لا يوجد طلاب مسجلون بعد") }
                else -> items(students.size) { i ->
                    val student = students[i]
                    AppCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarCircle(initial = student.name, size = 44)
                            Spacer(Modifier.size(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name, style = MaterialTheme.typography.titleSmall, color = Navy, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("الرقم الجامعي: ${student.id}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("المستوى ${student.currentLevel} - ترم ${student.currentTerm}") },
                                    shape = MaterialTheme.shapes.small
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "كلمة المرور: ${student.password}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                            IconButton(onClick = { deleteTarget = student }) {
                                Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AbsentRed)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "حذف الطالب",
            message = "هل تريد حذف الطالب \"${target.name}\"؟ سيتم حذف حساب الطالب وكل بياناته.",
            confirmText = "حذف",
            onConfirm = {
                scope.launch {
                    try {
                        AppService.repo.deleteStudent(target.id)
                        snackbarHostState.showSnackbar("تم حذف الطالب ${target.name}")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("فشل الحذف: ${e.message}")
                    }
                    deleteTarget = null
                    reload()
                }
            },
            onDismiss = { deleteTarget = null }
        )
    }

    importPreview?.let { rows ->
        AlertDialog(
            onDismissRequest = { if (!importing) importPreview = null },
            title = { Text("استيراد الطلاب", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("تم قراءة ${rows.size} طالب من الملف:", color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    rows.take(5).forEach { r ->
                        Text("• ${r.name} - ${r.id}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (rows.size > 5) Text("...", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !importing,
                    onClick = {
                        scope.launch {
                            importing = true
                            var added = 0
                            rows.forEach { row ->
                                try {
                                    AppService.repo.addStudent(row)
                                    added++
                                } catch (e: Exception) {
                                    // تخطي التكرارات أو الأخطاء
                                }
                            }
                            importing = false
                            importPreview = null
                            snackbarHostState.showSnackbar("تم استيراد $added من ${rows.size} طالب")
                            reload()
                        }
                    }
                ) {
                    Text("استيراد", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(enabled = !importing, onClick = { importPreview = null }) {
                    Text("إلغاء", color = TextSecondary)
                }
            },
            shape = MaterialTheme.shapes.large,
            containerColor = SurfaceWhite
        )
    }
}