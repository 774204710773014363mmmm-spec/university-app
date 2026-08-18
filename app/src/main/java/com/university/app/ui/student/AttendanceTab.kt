package com.university.app.ui.student

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.university.app.AppService
import com.university.app.data.model.Attendance
import com.university.app.data.model.AttendanceStatus
import com.university.app.data.model.User
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.EmptyState
import com.university.app.ui.components.LoadingBox
import com.university.app.ui.components.StatCard
import com.university.app.ui.theme.AbsentRed
import com.university.app.ui.theme.AbsentRedContainer
import com.university.app.ui.theme.Background
import com.university.app.ui.theme.Navy
import com.university.app.ui.theme.NavyContainer
import com.university.app.ui.theme.OnNavyContainer
import com.university.app.ui.theme.PresentGreen
import com.university.app.ui.theme.PresentGreenContainer
import com.university.app.ui.theme.SurfaceWhite
import com.university.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceTab(user: User?) {
    var step by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(0) }
    var term by remember { mutableIntStateOf(0) }
    var subject by remember { mutableStateOf("") }
    var subjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var records by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var showLockedDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val currentLevel = user?.currentLevel ?: 1

    fun loadSubjects(l: Int, t: Int) {
        scope.launch {
            loading = true
            subjects = try {
                AppService.repo.getSubjects(l, t)
            } catch (e: Exception) {
                emptyList()
            }
            loading = false
            step = 1
        }
    }

    fun loadRecords(s: String) {
        subject = s
        scope.launch {
            loading = true
            records = try {
                AppService.repo.getAttendance(user?.id ?: "", level, term, s)
            } catch (e: Exception) {
                emptyList()
            }
            loading = false
            step = 2
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (step > 0) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    when (step) {
                        1 -> step = 0
                        2 -> step = 1
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Navy)
                }
                Text(
                    text = when (step) {
                        1 -> "المستوى ${levelText(level)} > الترم ${termText(term)}"
                        else -> "$subject"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = Navy
                )
            }
        }

        when {
            loading -> LoadingBox()
            step == 0 -> AttendanceLevelGrid(
                currentLevel = currentLevel,
                onLevelClick = { l ->
                    if (l > currentLevel) {
                        showLockedDialog = true
                    } else {
                        level = l
                        step = 1
                    }
                }
            )
            step == 1 -> {
                if (subjects.isEmpty()) {
                    EmptyState("لا توجد مواد مسجلة لهذا المستوى والترم")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "اختر المادة",
                                style = MaterialTheme.typography.titleLarge,
                                color = Navy,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                        items(subjects.size) { i ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { loadRecords(subjects[i]) },
                                shape = MaterialTheme.shapes.large,
                                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(NavyContainer, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Event, contentDescription = null, tint = OnNavyContainer)
                                    }
                                    Spacer(Modifier.size(12.dp))
                                    Text(
                                        text = subjects[i],
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Navy,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> AttendanceDetail(records)
        }
    }

    if (showLockedDialog) {
        AlertDialog(
            onDismissRequest = { showLockedDialog = false },
            title = { Text("لم تصل لهذه المرحلة بعد", fontWeight = FontWeight.Bold) },
            text = { Text("ستتمكن من الاطلاع على سجل الحضور لهذه المرحلة عند انتقالك إليها.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showLockedDialog = false }) {
                    Text("حسناً", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = MaterialTheme.shapes.large,
            containerColor = SurfaceWhite
        )
    }
}

@Composable
private fun AttendanceLevelGrid(currentLevel: Int, onLevelClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Text(
                "اختر المستوى",
                style = MaterialTheme.typography.titleLarge,
                color = Navy,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        itemsIndexed(listOf(1, 2, 3, 4)) { _, lvl ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onLevelClick(lvl) },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = if (lvl > currentLevel) Background else SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (lvl > currentLevel) NavyContainer.copy(alpha = 0.6f) else NavyContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (lvl > currentLevel) Icons.Filled.School else Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (lvl > currentLevel) TextSecondary else OnNavyContainer
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = levelText(lvl),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (lvl > currentLevel) TextSecondary else Navy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceDetail(records: List<Attendance>) {
    val present = records.count { it.status == AttendanceStatus.PRESENT }
    val absent = records.size - present
    val percent = if (records.isEmpty()) 0f else (present.toFloat() / records.size) * 100f

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "إجمالي أيام الحضور",
                    value = "$present",
                    containerColor = PresentGreenContainer,
                    contentColor = PresentGreen,
                    icon = Icons.Filled.CheckCircle,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "إجمالي أيام الغياب",
                    value = "$absent",
                    containerColor = AbsentRedContainer,
                    contentColor = AbsentRed,
                    icon = Icons.Filled.Cancel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            AppCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نسبة الحضور", style = MaterialTheme.typography.titleSmall, color = Navy)
                        Text(
                            "${"%.0f".format(percent)}%",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (percent >= 75) PresentGreen else AbsentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        color = PresentGreen,
                        trackColor = AbsentRedContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "إجمالي المحاضرات: ${records.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }
        if (records.isEmpty()) {
            item { EmptyState("لا توجد سجلات حضور لهذه المادة بعد") }
        } else {
            item {
                Text(
                    "كشف المحاضرات",
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(records.size) { i ->
                val record = records[i]
                val isPresent = record.status == AttendanceStatus.PRESENT
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    if (isPresent) PresentGreenContainer else AbsentRedContainer,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPresent) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = if (isPresent) PresentGreen else AbsentRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "المحاضرة ${record.lectureNumber}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                            if (record.date.isNotBlank()) {
                                Spacer(Modifier.height(2.dp))
                                Text(record.date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            }
                        }
                        Text(
                            text = if (isPresent) "حاضر" else "غايب",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isPresent) PresentGreen else AbsentRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun levelText(level: Int): String = when (level) {
    1 -> "المستوى الأول"
    2 -> "المستوى الثاني"
    3 -> "المستوى الثالث"
    else -> "المستوى الرابع"
}

private fun termText(term: Int): String = if (term == 1) "الترم الأول" else "الترم الثاني"