package com.university.app.ui.admin

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.university.app.AppService
import com.university.app.data.model.Grade
import com.university.app.data.model.User
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.AppTopBar
import com.university.app.ui.components.DropdownField
import com.university.app.ui.components.LEVEL_LABELS
import com.university.app.ui.components.PrimaryButton
import com.university.app.ui.components.TERM_LABELS
import com.university.app.ui.components.levelNumber
import com.university.app.ui.components.termNumber
import com.university.app.ui.theme.Navy
import com.university.app.ui.theme.SurfaceWhite
import com.university.app.ui.theme.TextSecondary
import com.university.app.util.ExcelReader
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageGradesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var level by rememberSaveable { mutableStateOf("المستوى الأول") }
    var term by rememberSaveable { mutableStateOf("الترم الأول") }
    var subject by rememberSaveable { mutableStateOf("") }
    var units by rememberSaveable { mutableStateOf("4") }
    var subjectSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var subjectMenuOpen by remember { mutableStateOf(false) }

    var preview by remember { mutableStateOf<List<ExcelReader.GradeRow>?>(null) }
    var importing by remember { mutableStateOf(false) }
    var students by remember { mutableStateOf<List<User>>(emptyList()) }

    val excelLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val rows = try {
                    ExcelReader.readGrades(context, uri)
                } catch (e: Exception) {
                    snackbarHostState.showSnackbar("فشل قراءة الملف: ${e.message}")
                    emptyList()
                }
                if (rows.isNotEmpty()) preview = rows
                else snackbarHostState.showSnackbar("لم يتم العثور على درجات صالحة في الملف")
            }
        }
    }

    LaunchedEffect(level, term) {
        subjectSuggestions = try {
            AppService.repo.getSubjects(
                levelNumber(level),
                termNumber(term)
            )
        } catch (e: Exception) {
            emptyList()
        }
        students = try {
            AppService.repo.getStudents()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val unitsCount = units.toIntOrNull() ?: 0
    val canImport = subject.isNotBlank() && unitsCount > 0

    Scaffold(
        topBar = { AppTopBar(title = "إدارة الدرجات والوحدات", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
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
                            "استيراد الدرجات من ملف إكسل",
                            style = MaterialTheme.typography.titleMedium,
                            color = Navy,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "يجب أن يحتوي الملف على عمودين: الرقم الجامعي والدرجة. سيتم ربط كل درجة بالطالب في قاعدة البيانات بدقة.",
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

                        Column {
                            OutlinedTextField(
                                value = subject,
                                onValueChange = {
                                    subject = it
                                    subjectMenuOpen = true
                                },
                                label = { Text("اسم المادة") },
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            )
                            DropdownMenu(
                                expanded = subjectMenuOpen,
                                onDismissRequest = { subjectMenuOpen = false }
                            ) {
                                val filtered = subjectSuggestions
                                    .filter { subject.isBlank() || it.contains(subject.trim()) }
                                    .take(20)
                                if (filtered.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("لا توجد اقتراحات", color = TextSecondary) },
                                        onClick = { subjectMenuOpen = false }
                                    )
                                } else {
                                    filtered.forEach { s ->
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

                        OutlinedTextField(
                            value = units,
                            onValueChange = { units = it.filter { c -> c.isDigit() } },
                            label = { Text("عدد الوحدات الدراسية للمقرر") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(16.dp))

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
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(contentColor = Navy)
                        ) {
                            Icon(Icons.Filled.UploadFile, contentDescription = null)
                            Spacer(Modifier.size(8.dp))
                            Text("اختيار ملف إكسل من ذاكرة الهاتف", fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(10.dp))
                        PrimaryButton(
                            text = "رفع الدرجات إلى قاعدة البيانات",
                            enabled = canImport && preview != null,
                            onClick = {
                                val rows = preview ?: return@PrimaryButton
                                scope.launch {
                                    importing = true
                                    val records = rows.map { row ->
                                        Grade(
                                            studentId = row.studentId,
                                            level = levelNumber(level),
                                            term = termNumber(term),
                                            subjectName = subject.trim(),
                                            score = row.score,
                                            unitsCount = unitsCount
                                        )
                                    }
                                    try {
                                        val count = AppService.repo.importGrades(records)
                                        snackbarHostState.showSnackbar("تم رفع $count درجة بنجاح")
                                        preview = null
                                    } catch (e: Exception) {
                                        snackbarHostState.showSnackbar("فشل الرفع: ${e.message}")
                                    }
                                    importing = false
                                }
                            },
                            loading = importing,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    preview?.let { rows ->
        val nameById = students.associateBy { it.id }
        AlertDialog(
            onDismissRequest = { if (!importing) preview = null },
            title = { Text("معاينة الدرجات", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "تم قراءة ${rows.size} درجة للمادة \"${subject}\" (${unitsCount} وحدات):",
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    rows.take(5).forEach { r ->
                        Text(
                            "• ${r.studentId} (${nameById[r.studentId]?.name ?: "غير مسجل"}) : ${formatScore(r.score)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (rows.size > 5) Text("...", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !importing,
                    onClick = {
                        val recs = rows
                        scope.launch {
                            importing = true
                            val records = recs.map { row ->
                                Grade(
                                    studentId = row.studentId,
                                    level = levelNumber(level),
                                    term = termNumber(term),
                                    subjectName = subject.trim(),
                                    score = row.score,
                                    unitsCount = unitsCount
                                )
                            }
                            try {
                                val count = AppService.repo.importGrades(records)
                                snackbarHostState.showSnackbar("تم رفع $count درجة بنجاح")
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("فشل الرفع: ${e.message}")
                            }
                            importing = false
                            preview = null
                        }
                    }
                ) {
                    Text("رفع", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(enabled = !importing, onClick = { preview = null }) { Text("إلغاء", color = TextSecondary) }
            },
            shape = MaterialTheme.shapes.large,
            containerColor = SurfaceWhite
        )
    }
}

private fun formatScore(score: Double): String =
    if (score == score.toInt().toDouble()) score.toInt().toString() else "%.1f".format(score)