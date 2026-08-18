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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import com.university.app.data.model.Grade
import com.university.app.data.model.User
import com.university.app.ui.components.AppCard
import com.university.app.ui.components.EmptyState
import com.university.app.ui.components.LoadingBox
import com.university.app.ui.theme.Background
import com.university.app.ui.theme.Navy
import com.university.app.ui.theme.NavyContainer
import com.university.app.ui.theme.OnNavyContainer
import com.university.app.ui.theme.PresentGreen
import com.university.app.ui.theme.SurfaceWhite
import com.university.app.ui.theme.TextSecondary
import com.university.app.ui.theme.WarningAmber
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesTab(user: User?) {
    var step by remember { mutableIntStateOf(0) }
    var level by remember { mutableIntStateOf(0) }
    var term by remember { mutableIntStateOf(0) }
    var grades by remember { mutableStateOf<List<Grade>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var showLockedDialog by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<Grade?>(null) }
    val scope = rememberCoroutineScope()

    val currentLevel = user?.currentLevel ?: 1

    fun loadSubjects(l: Int, t: Int) {
        scope.launch {
            loading = true
            grades = try {
                AppService.repo.getGrades(user?.id ?: "", l, t)
            } catch (e: Exception) {
                emptyList()
            }
            loading = false
            step = 2
        }
    }

    fun back() {
        when (step) {
            1 -> step = 0
            2 -> step = 1
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (step > 0) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = ::back) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع", tint = Navy)
                }
                Text("الخطوة: ", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Text(
                    text = when (step) {
                        1 -> "المستوى ${levelText(level)}"
                        else -> "المستوى ${levelText(level)} > الترم ${termText(term)}"
                    },
                    style = MaterialTheme.typography.titleSmall,
                    color = Navy
                )
            }
        }

        when {
            loading -> LoadingBox()
            step == 0 -> LevelGrid(
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
            step == 1 -> TermGrid(
                onTermClick = { t ->
                    term = t
                    loadSubjects(level, t)
                }
            )
            else -> {
                if (grades.isEmpty()) {
                    EmptyState("لا توجد درجات مسجلة لهذه المادة بعد")
                } else {
                    val bySubject = grades.groupBy { it.subjectName }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "مواد المستوى ${levelText(level)} - الترم ${termText(term)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        bySubject.forEach { (subjectName, list) ->
                            val first = list.first()
                            item {
                                SubjectGradeCard(
                                    grade = first,
                                    onOpen = { selectedSubject = first }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLockedDialog) {
        AlertDialog(
            onDismissRequest = { showLockedDialog = false },
            title = { Text("لم تصل لهذه المرحلة بعد", fontWeight = FontWeight.Bold) },
            text = { Text("ستتمكن من الاطلاع على درجات هذه المرحلة عند انتقالك إليها.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = { showLockedDialog = false }) {
                    Text("حسناً", color = Navy, fontWeight = FontWeight.SemiBold)
                }
            },
            shape = MaterialTheme.shapes.large,
            containerColor = SurfaceWhite
        )
    }

    selectedSubject?.let { grade ->
        ModalBottomSheet(
            onDismissRequest = { selectedSubject = null },
            containerColor = SurfaceWhite,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            GradeDetailSheet(grade)
        }
    }
}

@Composable
private fun LevelGrid(currentLevel: Int, onLevelClick: (Int) -> Unit) {
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
            LevelCard(
                number = lvl,
                locked = lvl > currentLevel,
                onClick = { onLevelClick(lvl) }
            )
        }
    }
}

@Composable
private fun LevelCard(number: Int, locked: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = if (locked) Background else SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(if (locked) NavyContainer.copy(alpha = 0.6f) else NavyContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (locked) Icons.Filled.School else Icons.Filled.Star,
                    contentDescription = null,
                    tint = if (locked) TextSecondary else OnNavyContainer
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = levelText(number),
                style = MaterialTheme.typography.titleMedium,
                color = if (locked) TextSecondary else Navy,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = if (locked) "قريباً" else "اضغط للعرض",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun TermGrid(onTermClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Text(
                "اختر الترم",
                style = MaterialTheme.typography.titleLarge,
                color = Navy,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        itemsIndexed(listOf(1, 2)) { _, t ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onTermClick(t) },
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(NavyContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.School, contentDescription = null, tint = OnNavyContainer)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = termText(t),
                        style = MaterialTheme.typography.titleMedium,
                        color = Navy,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectGradeCard(grade: Grade, onOpen: () -> Unit) {
    AppCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = grade.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Navy,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${grade.unitsCount} وحدات دراسية",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            Card(
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = NavyContainer)
            ) {
                Text(
                    text = formatScore(grade.score),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnNavyContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun GradeDetailSheet(grade: Grade) {
    val weighted = grade.score * grade.unitsCount
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
        Text(
            text = grade.subjectName,
            style = MaterialTheme.typography.headlineSmall,
            color = Navy,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "تفاصيل الدرجة النهائية",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailTile(
                label = "الدرجة الحالية",
                value = formatScore(grade.score),
                container = NavyContainer,
                valueColor = OnNavyContainer,
                modifier = Modifier.weight(1f)
            )
            DetailTile(
                label = "عدد الوحدات",
                value = "${grade.unitsCount}",
                container = NavyContainer,
                valueColor = OnNavyContainer,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))
        DetailTile(
            label = "الدرجة النهائية (الدرجة × الوحدات)",
            value = formatWeighted(weighted),
            container = PresentGreen.copy(alpha = 0.12f),
            valueColor = PresentGreen,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "يتم حساب الدرجة النهائية تلقائياً بضرب درجة المادة في عدد وحداتها الدراسية.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun DetailTile(
    label: String,
    value: String,
    container: Color,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, color = valueColor, fontWeight = FontWeight.Bold)
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

private fun formatScore(score: Double): String =
    if (score == score.toInt().toDouble()) score.toInt().toString() else "%.1f".format(score)

private fun formatWeighted(weighted: Double): String =
    if (weighted == weighted.toInt().toDouble()) weighted.toInt().toString() else "%.1f".format(weighted)