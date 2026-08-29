package com.university.app.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.university.app.AppService
import com.university.app.data.model.Schedule
import com.university.app.data.model.User
import com.university.app.data.model.WeekDays
import com.university.app.ui.components.EmptyState
import com.university.app.ui.components.GlassCard
import com.university.app.ui.components.LoadingBox
import com.university.app.ui.components.SectionTitle
import com.university.app.ui.theme.GlassBorder
import com.university.app.ui.theme.GlowBlue
import com.university.app.ui.theme.GlowPurple
import com.university.app.ui.theme.TextSecondary

@Composable
fun ScheduleTab(user: User?) {
    var loading by remember { mutableStateOf(true) }
    var todayList by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var tomorrowList by remember { mutableStateOf<List<Schedule>>(emptyList()) }

    LaunchedEffect(user?.id) {
        loading = true
        val level = user?.currentLevel ?: 1
        val term = user?.currentTerm ?: 1
        todayList = try {
            AppService.repo.getSchedule(level, term, WeekDays.today())
        } catch (e: Exception) {
            emptyList()
        }
        tomorrowList = if (todayList.isEmpty()) {
            try {
                AppService.repo.getSchedule(level, term, WeekDays.nextOf(WeekDays.today()))
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
        loading = false
    }

    when {
        loading -> LoadingBox()
        todayList.isEmpty() && tomorrowList.isEmpty() -> EmptyState(
            "لا توجد محاضرات اليوم\nعند رفع الجدول من إدارة النظام سيظهر هنا تلقائياً"
        )
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (todayList.isNotEmpty()) {
                item {
                    SectionTitle("جدول اليوم (${WeekDays.today()})")
                    Spacer(Modifier.height(8.dp))
                }
                items(todayList.size) { i ->
                    ScheduleCard(todayList[i])
                }
            } else {
                item {
                    SectionTitle("لا توجد محاضرات اليوم، جدول الغد (${WeekDays.nextOf(WeekDays.today())})")
                    Spacer(Modifier.height(8.dp))
                }
                items(tomorrowList.size) { i ->
                    ScheduleCard(tomorrowList[i])
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ScheduleCard(schedule: Schedule) {
    GlassCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = schedule.subjectName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = GlowBlue,
                        modifier = Modifier.width(18.dp).height(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        schedule.time,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.MeetingRoom,
                        contentDescription = null,
                        tint = GlowPurple,
                        modifier = Modifier.width(18.dp).height(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "القاعة: ${schedule.hall}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary.copy(alpha = 0.8f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        tint = GlowBlue,
                        modifier = Modifier.width(18.dp).height(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        schedule.doctorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary.copy(alpha = 0.8f)
                    )
                }
            }
            SuggestionChip(
                onClick = {},
                label = { Text(schedule.day, fontSize = 12.sp) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = GlowBlue.copy(alpha = 0.15f),
                    labelColor = GlowBlue
                )
            )
        }
    }
}
