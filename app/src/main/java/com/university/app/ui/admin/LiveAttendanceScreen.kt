package com.university.app.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.university.app.AppService
import com.university.app.data.model.Attendance
import com.university.app.data.model.AttendanceStatus
import com.university.app.data.model.User
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.AppTopBar
import com.university.app.ui.components.ConfirmDialog
import com.university.app.ui.components.DropdownField
import com.university.app.ui.components.LEVEL_LABELS
import com.university.app.ui.components.PrimaryButton
import com.university.app.ui.components.TERM_LABELS
import com.university.app.ui.components.levelNumber
import com.university.app.ui.components.termNumber
import com.university.app.ui.theme.AbsentRed
import com.university.app.ui.theme.Background
import com.university.app.ui.theme.Navy
import com.university.app.ui.theme.PresentGreen
import com.university.app.ui.theme.SurfaceWhite
import com.university.app.ui.theme.TextSecondary
import com.university.app.util.ExcelReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveAttendanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var level by rememberSaveable { mutableStateOf("المستوى الأول") }
    var term by rememberSaveable { mutableStateOf("الترم الأول") }
    var subject by rememberSaveable { mutableStateOf("") }
    var lecture by rememberSaveable { mutableStateOf("1") }
    var subjectSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var subjectMenuOpen by remember { mutableStateOf(false) }

    var stage by rememberSaveable { mutableIntStateOf(0) }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }
    var index by rememberSaveable { mutableIntStateOf(0) }
    var saving by remember { mutableStateOf(false) }
    var showAbortDialog by remember { mutableStateOf(false) }
    var showDoneDialog by remember { mutableStateOf(false) }
    var presentCount by remember { mutableIntStateOf(0) }
    var absentCount by remember { mutableIntStateOf(0) }
    var importPreview by remember { mutableStateOf<List<ExcelReader.AttendanceRow>?>(null) }

    val levelNum = levelNumber(level)
    val termNum = termNumber(term)
    val lectureNum = lecture.toIntOrNull() ?: 1

    LaunchedEffect(level, term) {
        subjectSuggestions = try {
            AppService.repo.getSubjects(levelNum, termNum)
        } catch (e: Exception) {
            emptyList()
        }
    }

    LaunchedEffect(level, term, subject) {
        if (subject.isNotBlank()) {
            val last = try {
                AppService.repo.getLastLecture(levelNum, termNum, subject)
            } catch (e: Exception) {
                0
            }
            lecture = "${last + 1}"
        }
    }

    fun startCalling() {
        scope.launch {
            if (subject.isBlank()) {
                snackbarHostState.showSnackbar("أدخل اسم المادة أولاً")
                return@launch
            }
            val list = try {
                AppService.repo.getStudentsByLevel(levelNum)
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("فشل تحميل الطلاب: ${e.message}")
                return@launch
            }
            if (list.isEmpty()) {
                snackbarHostState.showSnackbar("لا يوجد طلاب مسجلون في هذا المستوى")
                return@launch
            }
            students = list
            index = 0
            presentCount = 0
            absentCount = 0
            stage = 1
        }
    }

    fun mark(status: String) {
        if (saving || students.isEmpty()) return
        val student = students[index]
        scope.launch {
            saving = true
            try {
                AppService.repo.markAttendance(
                    Attendance(
                        studentId = student.id,
                        level = levelNum,
                        term = termNum,
                        subjectName = subject.trim(),
                        lectureNumber = lectureNum,
                        status = status,
                        date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    )
                )
                if (status == AttendanceStatus.PRESENT) presentCount++ else absentCount++
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("فشل تسجيل الحالة: ${e.message}")
            }
            saving = false
            if (index + 1 >= students.size) {
                showDoneDialog = true
            } else {
                index++
            }
        }
    }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val rows = try {
                    ExcelReader.readAttendance(context, uri)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("فشل قراءة الملف: ${e.message}")
                    emptyList()
                }
                if (rows.isNotEmpty()) importPreview = rows
                else snackbarHostState.showSnackbar("لم يتم العثور على بيانات صالحة في الملف")
            }
        }
    }

    if (stage == 0) {
        Scaffold(
            topBar = { AppTopBar(title = "نظام الحضور التفاعلي", onBack = onBack) },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            containerColor = Background
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    AppCard {
                        Column {
                            Text(
                                "إعدادات المحاضرة",
                                style = MaterialTheme.typography.titleMedium,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
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
                            Spacer(Modifier.height(10.dp))
                            Column {
                                OutlinedButton(
                                    onClick = { subjectMenuOpen = true },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    shape = MaterialTheme.shapes.medium,
                                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                                ) {
                                    Text(
                                        if (subject.isBlank()) "اختر المادة..." else subject,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                DropdownMenu(
                                    expanded = subjectMenuOpen,
                                    onDismissRequest = { subjectMenuOpen = false }
                                ) {
                                    if (subjectSuggestions.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("لا توجد مواد مسجلة، أضفها من إدارة الجداول", color = TextSecondary) },
                                            onClick = { subjectMenuOpen = false }
                                        )
                                    } else {
                                        subjectSuggestions.forEach { s ->
                                            DropdownMenuItem(
                                                text = { Text(s) },
                                                onClick = {
                                                    subject = s
                                                    subjectMenuOpen = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
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
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = MaterialTheme.shapes.medium,
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                            ) {
                                Icon(Icons.Filled.UploadFile, contentDescription = null)
                                Spacer(Modifier.size(6.dp))
                                Text("استيراد كشوفات الحضور المبدئية (إكسل)", fontWeight = FontWeight.SemiBold)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "رقم المحاضرة: سيتم ضبطه تلقائياً وفق آخر محاضرة مسجلة",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "المحاضرة رقم $lecture",
                                style = MaterialTheme.typography.titleMedium,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton(
                                text = "بدء التحضير التفاعلي",
                                onClick = ::startCalling,
                                enabled = subject.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    } else {
        Scaffold(
            containerColor = Background,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { showAbortDialog = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "إنهاء", tint = TextSecondary)
                    }
                    Text(
                        "الطالب ${index + 1} من ${students.size}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy,
                        fontWeight = FontWeight.Bold
                    )
                    Card(
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite)
                    ) {
                        Text(
                            subject,
                            style = MaterialTheme.typography.labelMedium,
                            color = Navy,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "المحاضرة رقم $lecture",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(Navy.copy(alpha = 0.08f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = students.getOrNull(index)?.name?.ifEmpty { "؟" }?.take(1) ?: "؟",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(24.dp))
                        AnimatedContent(
                            targetState = index,
                            transitionSpec = {
                                fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                            },
                            label = "studentName"
                        ) { currentIndex ->
                            Text(
                                text = students.getOrNull(currentIndex)?.name ?: "",
                                style = MaterialTheme.typography.displaySmall,
                                color = Navy,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = students.getOrNull(index)?.id ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(24.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = PresentGreen.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    "حاضر: $presentCount",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PresentGreen,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(containerColor = AbsentRed.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    "غايب: $absentCount",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = AbsentRed,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Button(
                        onClick = { mark(AttendanceStatus.PRESENT) },
                        enabled = !saving,
                        modifier = Modifier.weight(1f).height(72.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = PresentGreen, contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("حاضر", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { mark(AttendanceStatus.ABSENT) },
                        enabled = !saving,
                        modifier = Modifier.weight(1f).height(72.dp),
                        shape = MaterialTheme.shapes.large,
                        colors = ButtonDefaults.buttonColors(containerColor = AbsentRed, contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.Cancel, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("غايب", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showAbortDialog) {
        ConfirmDialog(
            title = "إنهاء التحضير",
            message = "هل تريد إنهاء التحضير قبل اكتماله؟ لن يتم فقدان الحالات المسجلة حتى الآن.",
            confirmText = "إنهاء",
            onConfirm = {
                showAbortDialog = false
                stage = 0
                showDoneDialog = false
            },
            onDismiss = { showAbortDialog = false }
        )
    }

    if (showDoneDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = PresentGreen)
                    Spacer(Modifier.width(8.dp))
                    Text("اكتمل التحضير", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("تم اكتمال تحضير المحاضرة بنجاح", color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    Text("عدد الحاضرين: $presentCount", style = MaterialTheme.typography.bodyMedium, color = PresentGreen)
                    Text("عدد الغائبين: $absentCount", style = MaterialTheme.typography.bodyMedium, color = AbsentRed)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDoneDialog = false
                        stage = 0
                    }
                ) {
                    Text("رجوع للإعدادات", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = MaterialTheme.shapes.large,
            containerColor = SurfaceWhite
        )
    }

    importPreview?.let { rows ->
        AlertDialog(
            onDismissRequest = { importPreview = null },
            title = { Text("استيراد كشف الحضور", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "تم قراءة ${rows.size} سجل حضور للمادة \"${subject.ifBlank { "-" }}\":",
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    rows.take(5).forEach { r ->
                        Text(
                            "• ${r.studentId} - المحاضرة ${r.lectureNumber}: ${if (r.status == AttendanceStatus.PRESENT) "حاضر" else "غايب"}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (rows.size > 5) Text("...", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val records = rows.map { r ->
                                Attendance(
                                    studentId = r.studentId,
                                    level = levelNum,
                                    term = termNum,
                                    subjectName = subject.trim(),
                                    lectureNumber = r.lectureNumber,
                                    status = r.status,
                                    date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                                )
                            }
                            try {
                                val count = AppService.repo.importAttendance(records)
                                snackbarHostState.showSnackbar("تم استيراد $count سجل حضور")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("فشل الاستيراد: ${e.message}")
                            }
                            importPreview = null
                        }
                    }
                ) {
                    Text("استيراد", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { importPreview = null }) { Text("إلغاء", color = TextSecondary) }
            },
            shape = MaterialTheme.shapes.large,
            containerColor = SurfaceWhite
        )
    }
}