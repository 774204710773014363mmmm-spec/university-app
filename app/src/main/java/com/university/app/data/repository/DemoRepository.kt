package com.university.app.data.repository

import android.content.SharedPreferences
import com.university.app.data.model.Attendance
import com.university.app.data.model.AttendanceStatus
import com.university.app.data.model.Grade
import com.university.app.data.model.Role
import com.university.app.data.model.Schedule
import com.university.app.data.model.ScheduleRule
import com.university.app.data.model.User
import com.university.app.data.model.WeekDays
import org.json.JSONArray
import org.json.JSONObject

class DemoRepository(private val prefs: SharedPreferences) : AppRepository {

    private val users = linkedMapOf<String, User>()
    private val schedules = mutableListOf<Schedule>()
    private val grades = mutableListOf<Grade>()
    private val attendance = mutableListOf<Attendance>()

    private var adminPassword = "admin123"
    private val rules = defaultRules().toMutableList()

    init {
        load()
        if (users.isEmpty()) seed()
        save()
    }

    private fun defaultRules() = listOf(
        ScheduleRule(1, 1, "الرياضيات", "د. أحمد فؤاد", WeekDays.SATURDAY, "08:00", "قاعة A", 2),
        ScheduleRule(1, 1, "الفيزياء", "د. سامي عبدالله", WeekDays.SUNDAY, "10:00", "قاعة B", 2),
        ScheduleRule(1, 1, "البرمجة", "د. ليلى حسن", WeekDays.SUNDAY, "09:00", "معمل 1", 2),
        ScheduleRule(1, 1, "اللغة العربية", "د. منى خالد", WeekDays.MONDAY, "11:00", "قاعة C", 2),
        ScheduleRule(1, 1, "الإحصاء", "د. خالد عمر", WeekDays.MONDAY, "08:00", "قاعة A", 2)
    )

    private fun seed() {
        adminPassword = "admin123"
        users["admin"] = User(
            id = "admin", name = "المشرف العام", role = Role.ADMIN,
            phone = "", currentLevel = 1, currentTerm = 1, password = "admin123"
        )
        val names = listOf(
            "محمد أحمد السيد", "سارة خالد محمود", "عمر يوسف عبدالله", "نور الهدى صالح",
            "عبدالله سالم ناصر", "مريم فتحي حسن", "يوسف إبراهيم علي", "هدى عادل كامل",
            "خالد محمد رمضان", "أمل عبدالرحمن وليد"
        )
        names.forEachIndexed { i, name ->
            val num = 20240001 + i
            users[num.toString()] = User(
                id = num.toString(), name = name, role = Role.STUDENT,
                phone = "77${1000000 + i * 137}",
                currentLevel = if (i == 2) 2 else 1,
                currentTerm = 1,
                password = "123456"
            )
        }

        val subjects = listOf("الرياضيات", "الفيزياء", "البرمجة", "اللغة العربية", "الإحصاء")
        val units = mapOf("الرياضيات" to 4, "الفيزياء" to 4, "البرمجة" to 4, "اللغة العربية" to 3, "الإحصاء" to 3)
        val scores = mapOf(
            "الرياضيات" to 85.0, "الفيزياء" to 72.0, "البرمجة" to 90.0,
            "اللغة العربية" to 88.0, "الإحصاء" to 65.0
        )
        users.values.filter { it.role == Role.STUDENT }.forEach { u ->
            subjects.forEach { s ->
                val base = scores.getValue(s)
                val delta = ((u.id.lastOrNull()?.digitToIntOrNull() ?: 0) % 3 - 1) * 3
                grades += Grade(
                    studentId = u.id, level = 1, term = 1, subjectName = s,
                    score = (base + delta).coerceIn(50.0, 99.0),
                    unitsCount = units.getValue(s)
                )
            }
        }

        val daySubjects = mapOf(
            WeekDays.SATURDAY to listOf(
                Schedule(level = 1, term = 1, day = WeekDays.SATURDAY, subjectName = "الرياضيات", time = "08:00", hall = "قاعة A", doctorName = "د. أحمد فؤاد"),
                Schedule(level = 1, term = 1, day = WeekDays.SATURDAY, subjectName = "الفيزياء", time = "10:00", hall = "قاعة B", doctorName = "د. سامي عبدالله")
            ),
            WeekDays.SUNDAY to listOf(
                Schedule(level = 1, term = 1, day = WeekDays.SUNDAY, subjectName = "البرمجة", time = "09:00", hall = "معمل 1", doctorName = "د. ليلى حسن"),
                Schedule(level = 1, term = 1, day = WeekDays.SUNDAY, subjectName = "اللغة العربية", time = "11:00", hall = "قاعة C", doctorName = "د. منى خالد")
            ),
            WeekDays.MONDAY to listOf(
                Schedule(level = 1, term = 1, day = WeekDays.MONDAY, subjectName = "الإحصاء", time = "08:00", hall = "قاعة A", doctorName = "د. خالد عمر"),
                Schedule(level = 1, term = 1, day = WeekDays.MONDAY, subjectName = "الرياضيات", time = "12:00", hall = "قاعة A", doctorName = "د. أحمد فؤاد")
            ),
            WeekDays.TUESDAY to listOf(
                Schedule(level = 1, term = 1, day = WeekDays.TUESDAY, subjectName = "الفيزياء", time = "09:00", hall = "قاعة B", doctorName = "د. سامي عبدالله"),
                Schedule(level = 1, term = 1, day = WeekDays.TUESDAY, subjectName = "البرمجة", time = "11:00", hall = "معمل 1", doctorName = "د. ليلى حسن")
            ),
            WeekDays.WEDNESDAY to listOf(
                Schedule(level = 1, term = 1, day = WeekDays.WEDNESDAY, subjectName = "اللغة العربية", time = "10:00", hall = "قاعة C", doctorName = "د. منى خالد"),
                Schedule(level = 1, term = 1, day = WeekDays.WEDNESDAY, subjectName = "الإحصاء", time = "12:00", hall = "قاعة A", doctorName = "د. خالد عمر")
            )
        )
        daySubjects.values.flatten().forEach { schedules += it }

        attendance += Attendance(
            studentId = "20240001", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 1, status = AttendanceStatus.PRESENT, date = "2026-08-08"
        )
        attendance += Attendance(
            studentId = "20240001", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 2, status = AttendanceStatus.PRESENT, date = "2026-08-10"
        )
        attendance += Attendance(
            studentId = "20240001", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 3, status = AttendanceStatus.ABSENT, date = "2026-08-12"
        )
        attendance += Attendance(
            studentId = "20240001", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 4, status = AttendanceStatus.PRESENT, date = "2026-08-15"
        )
        attendance += Attendance(
            studentId = "20240002", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 1, status = AttendanceStatus.ABSENT, date = "2026-08-08"
        )
        attendance += Attendance(
            studentId = "20240002", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 2, status = AttendanceStatus.PRESENT, date = "2026-08-10"
        )
        attendance += Attendance(
            studentId = "20240003", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 1, status = AttendanceStatus.PRESENT, date = "2026-08-08"
        )
        attendance += Attendance(
            studentId = "20240003", level = 1, term = 1, subjectName = "الرياضيات",
            lectureNumber = 2, status = AttendanceStatus.PRESENT, date = "2026-08-10"
        )
    }

    private fun load() {
        users.clear(); schedules.clear(); grades.clear(); attendance.clear()
        val usersJson = prefs.getString(KEY_USERS, null)
        if (!usersJson.isNullOrEmpty()) {
            val arr = JSONArray(usersJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                users[obj.getString("id")] = User.fromMap(obj.getString("id"), jsonToMap(obj))
            }
        }
        val schedJson = prefs.getString(KEY_SCHEDULES, null)
        if (!schedJson.isNullOrEmpty()) {
            val arr = JSONArray(schedJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                schedules += Schedule.fromMap(obj.optString("id"), jsonToMap(obj))
            }
        }
        val gradesJson = prefs.getString(KEY_GRADES, null)
        if (!gradesJson.isNullOrEmpty()) {
            val arr = JSONArray(gradesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                grades += Grade.fromMap(obj.optString("id"), jsonToMap(obj))
            }
        }
        val attJson = prefs.getString(KEY_ATTENDANCE, null)
        if (!attJson.isNullOrEmpty()) {
            val arr = JSONArray(attJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                attendance += Attendance.fromMap(obj.optString("id"), jsonToMap(obj))
            }
        }
        adminPassword = prefs.getString(KEY_ADMIN_PW, "admin123") ?: "admin123"
    }

    private fun save() {
        val usersArr = JSONArray()
        users.values.forEach { u ->
            val obj = JSONObject()
            obj.put("id", u.id)
            mapToJson(u.toMap(), obj)
            usersArr.put(obj)
        }
        val schedArr = JSONArray()
        schedules.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            mapToJson(s.toMap(), obj)
            schedArr.put(obj)
        }
        val gradesArr = JSONArray()
        grades.forEach { g ->
            val obj = JSONObject()
            obj.put("id", g.id)
            mapToJson(g.toMap(), obj)
            gradesArr.put(obj)
        }
        val attArr = JSONArray()
        attendance.forEach { a ->
            val obj = JSONObject()
            obj.put("id", a.id)
            mapToJson(a.toMap(), obj)
            attArr.put(obj)
        }
        prefs.edit()
            .putString(KEY_USERS, usersArr.toString())
            .putString(KEY_SCHEDULES, schedArr.toString())
            .putString(KEY_GRADES, gradesArr.toString())
            .putString(KEY_ATTENDANCE, attArr.toString())
            .putString(KEY_ADMIN_PW, adminPassword)
            .apply()
    }

    private fun mapToJson(map: Map<String, Any>, obj: JSONObject) {
        map.forEach { (k, v) ->
            when (v) {
                is String -> obj.put(k, v)
                is Int -> obj.put(k, v)
                is Double -> obj.put(k, v)
                is Boolean -> obj.put(k, v)
                is Long -> obj.put(k, v)
            }
        }
    }

    private fun jsonToMap(obj: JSONObject): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        obj.keys().forEach { k ->
            if (k != "id") {
                val v = obj.get(k)
                if (v == JSONObject.NULL) return@forEach
                map[k] = v
            }
        }
        return map
    }

    override suspend fun login(identifier: String, password: String): User? {
        val id = identifier.trim()
        if (id.isEmpty()) return null
        return users[id]?.takeIf { it.password == password }
            ?: users.values.firstOrNull { it.phone == id }?.takeIf { it.password == password }
    }

    override suspend fun getUser(userId: String): User? = users[userId]

    override suspend fun checkAdminPassword(password: String): Boolean = password == adminPassword

    override suspend fun addStudent(user: User) {
        users[user.id] = user
        save()
    }

    override suspend fun deleteStudent(studentId: String) {
        users.remove(studentId)
        grades.removeAll { it.studentId == studentId }
        attendance.removeAll { it.studentId == studentId }
        save()
    }

    override suspend fun getStudents(): List<User> =
        users.values.filter { it.role == Role.STUDENT }.sortedBy { it.id }

    override suspend fun getStudentsByLevel(level: Int): List<User> =
        users.values.filter { it.role == Role.STUDENT && it.currentLevel == level }.sortedBy { it.id }

    override suspend fun getSchedule(level: Int, term: Int, day: String): List<Schedule> =
        schedules.filter { it.level == level && it.term == term && it.day == day }.sortedBy { it.time }

    override suspend fun getAllSchedules(): List<Schedule> = schedules.sortedBy { it.day }

    override suspend fun addSchedule(schedule: Schedule) {
        schedules += schedule.copy(id = "s${System.currentTimeMillis()}${schedules.size}")
        save()
    }

    override suspend fun deleteSchedule(id: String) {
        schedules.removeAll { it.id == id }
        save()
    }

    override suspend fun clearSchedules() {
        schedules.clear()
        save()
    }

    override suspend fun generateAutoSchedule(): Int {
        schedules.clear()
        var count = 0
        rules.forEach { rule ->
            repeat(rule.sessions) {
                schedules += Schedule(
                    id = "s${System.currentTimeMillis()}${count}",
                    level = rule.level, term = rule.term, day = rule.day,
                    subjectName = rule.subjectName, time = rule.time,
                    hall = rule.hall, doctorName = rule.doctorName
                )
                count++
            }
        }
        save()
        return count
    }

    override suspend fun getSubjects(level: Int, term: Int): List<String> {
        val subjects = mutableSetOf<String>()
        schedules.filter { it.level == level && it.term == term }.forEach { subjects.add(it.subjectName) }
        grades.filter { it.level == level && it.term == term }.forEach { subjects.add(it.subjectName) }
        return subjects.sorted()
    }

    override suspend fun getGrades(studentId: String, level: Int, term: Int): List<Grade> =
        grades.filter { it.studentId == studentId && it.level == level && it.term == term }
            .sortedBy { it.subjectName }

    override suspend fun importGrades(records: List<Grade>): Int {
        records.forEach { g ->
            grades += g.copy(id = "g${System.currentTimeMillis()}${grades.size}")
        }
        save()
        return records.size
    }

    override suspend fun getAttendance(
        studentId: String,
        level: Int,
        term: Int,
        subject: String
    ): List<Attendance> =
        attendance.filter {
            it.studentId == studentId && it.level == level && it.term == term && it.subjectName == subject
        }.sortedBy { it.lectureNumber }

    override suspend fun getLastLecture(level: Int, term: Int, subject: String): Int =
        attendance.filter { it.level == level && it.term == term && it.subjectName == subject }
            .maxOfOrNull { it.lectureNumber } ?: 0

    override suspend fun markAttendance(record: Attendance) {
        attendance += record.copy(id = "a${System.currentTimeMillis()}${attendance.size}")
        save()
    }

    override suspend fun importAttendance(records: List<Attendance>): Int {
        records.forEach { a ->
            attendance += a.copy(id = "a${System.currentTimeMillis()}${attendance.size}")
        }
        save()
        return records.size
    }

    companion object {
        private const val KEY_USERS = "users"
        private const val KEY_SCHEDULES = "schedules"
        private const val KEY_GRADES = "grades"
        private const val KEY_ATTENDANCE = "attendance"
        private const val KEY_ADMIN_PW = "admin_pw"
    }
}