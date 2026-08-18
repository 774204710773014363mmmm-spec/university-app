package com.university.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.university.app.data.model.Attendance
import com.university.app.data.model.Grade
import com.university.app.data.model.Role
import com.university.app.data.model.Schedule
import com.university.app.data.model.ScheduleRule
import com.university.app.data.model.User
import kotlinx.coroutines.tasks.await

class FirestoreRepository : AppRepository {

    private val db = FirebaseFirestore.getInstance()
    private val usersCol = db.collection("users")
    private val schedulesCol = db.collection("schedules")
    private val gradesCol = db.collection("grades")
    private val attendanceCol = db.collection("attendance")
    private val settingsDoc = db.document("settings/admin")
    private val rulesDoc = db.document("settings/scheduleRules")

    private val defaultAdminPassword = "admin123"

    private val defaultRules = listOf(
        ScheduleRule(1, 1, "الرياضيات", "د. أحمد فؤاد", "السبت", "08:00", "قاعة A", 2),
        ScheduleRule(1, 1, "الفيزياء", "د. سامي عبدالله", "الأحد", "10:00", "قاعة B", 2),
        ScheduleRule(1, 1, "البرمجة", "د. ليلى حسن", "الأحد", "09:00", "معمل 1", 2),
        ScheduleRule(1, 1, "اللغة العربية", "د. منى خالد", "الاثنين", "11:00", "قاعة C", 2),
        ScheduleRule(1, 1, "الإحصاء", "د. خالد عمر", "الاثنين", "08:00", "قاعة A", 2)
    )

    override suspend fun login(identifier: String, password: String): User? {
        val id = identifier.trim()
        if (id.isEmpty()) return null
        val doc = usersCol.document(id).get().await()
        var user: User? = if (doc.exists()) User.fromMap(doc.id, doc.data ?: emptyMap()) else null
        if (user == null) {
            val byPhone = usersCol.whereEqualTo("phone", id).get().await()
            if (!byPhone.isEmpty) {
                val d = byPhone.documents[0]
                user = User.fromMap(d.id, d.data ?: emptyMap())
            }
        }
        return user?.takeIf { it.password == password }
    }

    override suspend fun getUser(userId: String): User? {
        val doc = usersCol.document(userId).get().await()
        return if (doc.exists()) User.fromMap(doc.id, doc.data ?: emptyMap()) else null
    }

    override suspend fun checkAdminPassword(password: String): Boolean {
        val doc = settingsDoc.get().await()
        val stored = if (doc.exists()) (doc.getString("password") ?: defaultAdminPassword) else defaultAdminPassword
        return password == stored
    }

    override suspend fun addStudent(user: User) {
        usersCol.document(user.id).set(user.toMap()).await()
    }

    override suspend fun deleteStudent(studentId: String) {
        usersCol.document(studentId).delete().await()
    }

    override suspend fun getStudents(): List<User> {
        val snap = usersCol.whereEqualTo("role", Role.STUDENT).get().await()
        return snap.documents.map { User.fromMap(it.id, it.data ?: emptyMap()) }.sortedBy { it.id }
    }

    override suspend fun getStudentsByLevel(level: Int): List<User> {
        val snap = usersCol
            .whereEqualTo("role", Role.STUDENT)
            .whereEqualTo("currentLevel", level)
            .get().await()
        return snap.documents.map { User.fromMap(it.id, it.data ?: emptyMap()) }.sortedBy { it.id }
    }

    override suspend fun getSchedule(level: Int, term: Int, day: String): List<Schedule> {
        val snap = schedulesCol
            .whereEqualTo("level", level)
            .whereEqualTo("term", term)
            .whereEqualTo("day", day)
            .get().await()
        return snap.documents
            .map { Schedule.fromMap(it.id, it.data ?: emptyMap()) }
            .sortedBy { it.time }
    }

    override suspend fun getAllSchedules(): List<Schedule> {
        val snap = schedulesCol.get().await()
        return snap.documents
            .map { Schedule.fromMap(it.id, it.data ?: emptyMap()) }
            .sortedBy { it.day }
    }

    override suspend fun addSchedule(schedule: Schedule) {
        schedulesCol.add(schedule.toMap()).await()
    }

    override suspend fun deleteSchedule(id: String) {
        schedulesCol.document(id).delete().await()
    }

    override suspend fun clearSchedules() {
        val snap = schedulesCol.get().await()
        val batch = db.batch()
        snap.documents.forEach { batch.delete(it.reference) }
        if (snap.size() > 0) batch.commit().await()
    }

    override suspend fun generateAutoSchedule(): Int {
        val rules = readRules()
        clearSchedules()
        val batch = db.batch()
        var count = 0
        rules.forEach { rule ->
            repeat(rule.sessions) {
                batch.set(
                    schedulesCol.document(),
                    Schedule(
                        level = rule.level,
                        term = rule.term,
                        day = rule.day,
                        subjectName = rule.subjectName,
                        time = rule.time,
                        hall = rule.hall,
                        doctorName = rule.doctorName
                    ).toMap()
                )
                count++
            }
        }
        if (count > 0) batch.commit().await()
        return count
    }

    private suspend fun readRules(): List<ScheduleRule> {
        val doc = rulesDoc.get().await()
        val raw = if (doc.exists()) doc.get("rules") else null
        val rules = (raw as? List<*>)?.mapNotNull { item ->
            (item as? Map<*, *>)?.let { m ->
                m.entries.associate { it.key.toString() to it.value }.let {
                    ScheduleRule.fromMap(it)
                }
            }
        } ?: emptyList()
        return rules.ifEmpty { defaultRules }
    }

    override suspend fun getSubjects(level: Int, term: Int): List<String> {
        val subjects = mutableSetOf<String>()
        schedulesCol.whereEqualTo("level", level).whereEqualTo("term", term).get().await()
            .documents.forEach { d -> d.getString("subjectName")?.let { subjects.add(it) } }
        gradesCol.whereEqualTo("level", level).whereEqualTo("term", term).get().await()
            .documents.forEach { d -> d.getString("subjectName")?.let { subjects.add(it) } }
        return subjects.sorted()
    }

    override suspend fun getGrades(studentId: String, level: Int, term: Int): List<Grade> {
        val snap = gradesCol
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("level", level)
            .whereEqualTo("term", term)
            .get().await()
        return snap.documents
            .map { Grade.fromMap(it.id, it.data ?: emptyMap()) }
            .sortedBy { it.subjectName }
    }

    override suspend fun importGrades(records: List<Grade>): Int {
        val batch = db.batch()
        records.forEach { batch.set(gradesCol.document(), it.toMap()) }
        if (records.isNotEmpty()) batch.commit().await()
        return records.size
    }

    override suspend fun getAttendance(
        studentId: String,
        level: Int,
        term: Int,
        subject: String
    ): List<Attendance> {
        val snap = attendanceCol
            .whereEqualTo("studentId", studentId)
            .whereEqualTo("level", level)
            .whereEqualTo("term", term)
            .whereEqualTo("subjectName", subject)
            .get().await()
        return snap.documents
            .map { Attendance.fromMap(it.id, it.data ?: emptyMap()) }
            .sortedBy { it.lectureNumber }
    }

    override suspend fun getLastLecture(level: Int, term: Int, subject: String): Int {
        val snap = attendanceCol
            .whereEqualTo("level", level)
            .whereEqualTo("term", term)
            .whereEqualTo("subjectName", subject)
            .get().await()
        return snap.documents
            .mapNotNull { (it.get("lectureNumber") as? Number)?.toInt() }
            .maxOrNull() ?: 0
    }

    override suspend fun markAttendance(record: Attendance) {
        attendanceCol.add(record.toMap()).await()
    }

    override suspend fun importAttendance(records: List<Attendance>): Int {
        val batch = db.batch()
        records.forEach { batch.set(attendanceCol.document(), it.toMap()) }
        if (records.isNotEmpty()) batch.commit().await()
        return records.size
    }
}