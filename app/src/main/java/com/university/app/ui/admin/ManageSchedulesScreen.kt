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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.mutableIntStateOf
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
import com.university.app.data.model.Schedule
import com.university.app.data.model.WeekDays
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.AppTopBar
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
import com.university.app.ui.theme.NavyContainer
import com.university.app.ui.theme.OnNavyContainer
import com.university.app.ui.theme.SurfaceWhite
import com.university.app.ui.theme.TextSecondary
import com.university.app.util.ExcelReader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSchedulesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var mode by rememberSaveable { mutableIntStateOf(0) }
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var generating by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Schedule?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var importPreview by remember { mutableStateOf<List<Schedule>?>(null) }

    var level by rememberSaveable { mutableStateOf("المستوى الأول") }
    var term by rememberSaveable { mutableStateOf("الترم الأول") }
    var day by rememberSaveable { mutableStateOf(WeekDays.SATURDAY) }
    var subject by rememberSaveable { mutableStateOf("") }
    var time by rememberSaveable { mutableStateOf("") }
    var hall by rememberSaveable { mutableStateOf("") }
    var doctor by rememberSaveable { mutableStateOf("") }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val rows = try {
                    ExcelReader.readSchedules(context, uri)
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
            schedules = try {
                AppService.repo.getAllSchedules()
            } catch (e: Exception) {
                emptyList()
            }
            loading = false
        }
    }

    LaunchedEffect(Unit) { reload() }

    fun addSchedule() {
        if (subject.isBlank() || time.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("اسم المادة والوقت مطلوبان") }
            return
        }
        scope.launch {
            val schedule = Schedule(
                level = levelNumber(level),
                term = termNumber(term),
                day = day,
                subjectName = subject.trim(),
                time = time.trim(),
                hall = hall.trim(),
                doctorName = doctor.trim()
            )
            try {
                AppService.repo.addSchedule(schedule)
                snackbarHostState.showSnackbar("تمت إضافة المحاضرة بنجاح")
                subject = ""; time = ""; hall = ""; doctor = ""
                reload()
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("فشل الإضافة: ${e.message}")
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "إدارة الجداول اليومية", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = mode == 0,
                        onClick = { mode = 0 },
                        label = { Text("الجدول التلقائي") },
                        leadingIcon = {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                    FilterChip(
                        selected = mode == 1,
                        onClick = { mode = 1 },
                        label = { Text("الرفع اليدوي") }
                    )
                }
            }

            if (mode == 0) {
                item {
                    AppCard {
                        Column {
                            Text(
                                "الجدول التلقائي",
                                style = MaterialTheme.typography.titleMedium,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "يولّد النظام الجدول تلقائياً بناءً على القواعد المخزنة في قاعدة البيانات (مادة، يوم، وقت، قاعة، دكتور، عدد الجلسات). يمكنك إضافة أو تعديل القواعد من قاعدة البيانات مباشرة.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton(
                                text = "توليد الجدول الآن",
                                loading = generating,
                                onClick = {
                                    scope.launch {
                                        generating = true
                                        try {
                                            val count = AppService.repo.generateAutoSchedule()
                                            snackbarHostState.showSnackbar("تم توليد $count محاضرة بنجاح")
                                            reload()
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("فشل التوليد: ${e.message}")
                                        }
                                        generating = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                item {
                    AppCard {
                        Column {
                            Text("إضافة محاضرة يدوياً", style = MaterialTheme.typography.titleMedium, color = Navy, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "الجدول المرفوع يدوياً يظهر فوراً للطلاب ويلغي الجدول التلقائي",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
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
                            DropdownField(
                                label = "اليوم",
                                options = WeekDays.all,
                                selected = day,
                                onSelect = { day = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("اسم المادة") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = time,
                                    onValueChange = { time = it },
                                    label = { Text("الوقت (مثال: 08:00)") },
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = hall,
                                    onValueChange = { hall = it },
                                    label = { Text("رقم القاعة") },
                                    shape = MaterialTheme.shapes.medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = doctor,
                                onValueChange = { doctor = it },
                                label = { Text("اسم الدكتور") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            PrimaryButton(text = "إضافة المحاضرة", onClick = ::addSchedule, modifier = Modifier.fillMaxWidth())
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
                                Text("رفع جدول كامل من ملف إكسل", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionTitle("المحاضرات الحالية (${schedules.size})")
                    TextButton(onClick = { if (schedules.isNotEmpty()) showClearDialog = true }) {
                        Text("حذف الكل", color = AbsentRed)
                    }
                }
            }

            when {
                loading -> item { LoadingBox() }
                schedules.isEmpty() -> item { EmptyState("لا توجد محاضرات في الجدول") }
                else -> {
                    val grouped = schedules.groupBy { it.day }
                    WeekDays.all.forEach { d ->
                        val daySchedules = grouped[d].orEmpty()
                        if (daySchedules.isNotEmpty()) {
                            item {
                                Text(
                                    d,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Navy,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            items(daySchedules.size) { i ->
                                val s = daySchedules[i]
                                AppCard {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${s.subjectName} - ${s.time}",
                                                style = MaterialTheme.typography.titleSmall,
                                                color = Navy,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "المستوى ${s.level} - ترم ${s.term} | قاعة: ${s.hall} | ${s.doctorName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextSecondary
                                            )
                                        }
                                        IconButton(onClick = { deleteTarget = s }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "حذف", tint = AbsentRed)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }

    deleteTarget?.let { target ->
        ConfirmDialog(
            title = "حذف المحاضرة",
            message = "حذف محاضرة \"${target.subjectName}\" يوم ${target.day} الساعة ${target.time}؟",
            confirmText = "حذف",
            onConfirm = {
                scope.launch {
                    try {
                        AppService.repo.deleteSchedule(target.id)
                        snackbarHostState.showSnackbar("تم حذف المحاضرة")
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

    if (showClearDialog) {
        ConfirmDialog(
            title = "حذف جميع المحاضرات",
            message = "سيتم حذف كل محاضرات الجدول الحالية. هل أنت متأكد؟",
            confirmText = "حذف الكل",
            onConfirm = {
                scope.launch {
                    try {
                        AppService.repo.clearSchedules()
                        snackbarHostState.showSnackbar("تم حذف جميع المحاضرات")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("فشل الحذف: ${e.message}")
                    }
                    showClearDialog = false
                    reload()
                }
            },
            onDismiss = { showClearDialog = false }
        )
    }

    importPreview?.let { rows ->
        AlertDialog(
            onDismissRequest = { importPreview = null },
            title = { Text("استيراد الجدول", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("تم قراءة ${rows.size} محاضرة من الملف:", color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    rows.take(5).forEach { r ->
                        Text("• ${r.subjectName} - ${r.day} ${r.time}", style = MaterialTheme.typography.bodyMedium)
                    }
                    if (rows.size > 5) Text("...", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            var added = 0
                            rows.forEach { row ->
                                try {
                                    AppService.repo.addSchedule(row)
                                    added++
                                } catch (e: Exception) {
                                }
                            }
                            importPreview = null
                            snackbarHostState.showSnackbar("تم استيراد $added محاضرة")
                            reload()
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