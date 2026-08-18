package com.university.app.ui.admin

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.university.app.ui.components.AppTopBar
import com.university.app.ui.theme.Background
import com.university.app.ui.theme.Navy
import com.university.app.ui.theme.NavyContainer
import com.university.app.ui.theme.OnNavyContainer
import com.university.app.ui.theme.SurfaceWhite
import com.university.app.ui.theme.TextSecondary

private data class AdminSection(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onStudents: () -> Unit,
    onSchedules: () -> Unit,
    onGrades: () -> Unit,
    onAttendance: () -> Unit
) {
    val sections = listOf(
        AdminSection("إدارة حسابات الطلاب", "إضافة الطلاب يدوياً أو استيرادهم من ملف إكسل", Icons.Filled.Person),
        AdminSection("إدارة الجداول اليومية", "توليد الجدول تلقائياً أو رفعه يدوياً", Icons.Filled.CalendarMonth),
        AdminSection("إدارة الدرجات والوحدات", "استيراد الدرجات من إكسل مع تحديد الوحدات", Icons.Filled.Grade),
        AdminSection("نظام الحضور التفاعلي", "تحضير مباشر للطلاب واستيراد كشوفات", Icons.Filled.FactCheck)
    )

    Scaffold(
        topBar = { AppTopBar(title = "لوحة تحكم المشرف", onBack = onBack) },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    "أقسام لوحة التحكم",
                    style = MaterialTheme.typography.titleLarge,
                    color = Navy,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            items(sections.size) { i ->
                val section = sections[i]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (i) {
                                0 -> onStudents()
                                1 -> onSchedules()
                                2 -> onGrades()
                                else -> onAttendance()
                            }
                        },
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(NavyContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(section.icon, contentDescription = null, tint = OnNavyContainer, modifier = Modifier.size(26.dp))
                        }
                        Spacer(Modifier.size(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                section.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = Navy,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(section.description, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Icon(
                            Icons.Filled.ChevronLeft,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}