package com.university.app.data.model

object Role {
    const val STUDENT = "STUDENT"
    const val ADMIN = "ADMIN"
}

object AttendanceStatus {
    const val PRESENT = "present"
    const val ABSENT = "absent"

    fun fromRaw(raw: String): String {
        val r = raw.trim().lowercase()
        return when {
            r == "present" || r == "p" || r == "h" || r == "حاضر" || r == "حضور" || r == "نعم" || r == "1" -> PRESENT
            else -> ABSENT
        }
    }
}

data class User(
    val id: String = "",
    val name: String = "",
    val role: String = Role.STUDENT,
    val phone: String = "",
    val currentLevel: Int = 1,
    val currentTerm: Int = 1,
    val password: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "name" to name,
        "role" to role,
        "phone" to phone,
        "currentLevel" to currentLevel,
        "currentTerm" to currentTerm,
        "password" to password
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): User = User(
            id = id,
            name = (map["name"] as? String) ?: "",
            role = (map["role"] as? String) ?: Role.STUDENT,
            phone = (map["phone"] as? String) ?: "",
            currentLevel = ((map["currentLevel"] as? Number)?.toInt()) ?: 1,
            currentTerm = ((map["currentTerm"] as? Number)?.toInt()) ?: 1,
            password = (map["password"] as? String) ?: ""
        )
    }
}

data class Schedule(
    val id: String = "",
    val level: Int = 1,
    val term: Int = 1,
    val day: String = "",
    val subjectName: String = "",
    val time: String = "",
    val hall: String = "",
    val doctorName: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "level" to level,
        "term" to term,
        "day" to day,
        "subjectName" to subjectName,
        "time" to time,
        "hall" to hall,
        "doctorName" to doctorName
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Schedule = Schedule(
            id = id,
            level = ((map["level"] as? Number)?.toInt()) ?: 1,
            term = ((map["term"] as? Number)?.toInt()) ?: 1,
            day = (map["day"] as? String) ?: "",
            subjectName = (map["subjectName"] as? String) ?: "",
            time = (map["time"] as? String) ?: "",
            hall = (map["hall"] as? String) ?: "",
            doctorName = (map["doctorName"] as? String) ?: ""
        )
    }
}

data class Grade(
    val id: String = "",
    val studentId: String = "",
    val level: Int = 1,
    val term: Int = 1,
    val subjectName: String = "",
    val score: Double = 0.0,
    val unitsCount: Int = 4
) {
    fun toMap(): Map<String, Any> = mapOf(
        "studentId" to studentId,
        "level" to level,
        "term" to term,
        "subjectName" to subjectName,
        "score" to score,
        "unitsCount" to unitsCount
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Grade = Grade(
            id = id,
            studentId = (map["studentId"] as? String) ?: "",
            level = ((map["level"] as? Number)?.toInt()) ?: 1,
            term = ((map["term"] as? Number)?.toInt()) ?: 1,
            subjectName = (map["subjectName"] as? String) ?: "",
            score = ((map["score"] as? Number)?.toDouble()) ?: 0.0,
            unitsCount = ((map["unitsCount"] as? Number)?.toInt()) ?: 4
        )
    }
}

data class Attendance(
    val id: String = "",
    val studentId: String = "",
    val level: Int = 1,
    val term: Int = 1,
    val subjectName: String = "",
    val lectureNumber: Int = 1,
    val status: String = AttendanceStatus.PRESENT,
    val date: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "studentId" to studentId,
        "level" to level,
        "term" to term,
        "subjectName" to subjectName,
        "lectureNumber" to lectureNumber,
        "status" to status,
        "date" to date
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any>): Attendance = Attendance(
            id = id,
            studentId = (map["studentId"] as? String) ?: "",
            level = ((map["level"] as? Number)?.toInt()) ?: 1,
            term = ((map["term"] as? Number)?.toInt()) ?: 1,
            subjectName = (map["subjectName"] as? String) ?: "",
            lectureNumber = ((map["lectureNumber"] as? Number)?.toInt()) ?: 1,
            status = (map["status"] as? String) ?: AttendanceStatus.PRESENT,
            date = (map["date"] as? String) ?: ""
        )
    }
}

data class ScheduleRule(
    val level: Int = 1,
    val term: Int = 1,
    val subjectName: String = "",
    val doctorName: String = "",
    val day: String = "",
    val time: String = "",
    val hall: String = "",
    val sessions: Int = 2
) {
    fun toMap(): Map<String, Any> = mapOf(
        "level" to level,
        "term" to term,
        "subjectName" to subjectName,
        "doctorName" to doctorName,
        "day" to day,
        "time" to time,
        "hall" to hall,
        "sessions" to sessions
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): ScheduleRule = ScheduleRule(
            level = ((map["level"] as? Number)?.toInt()) ?: 1,
            term = ((map["term"] as? Number)?.toInt()) ?: 1,
            subjectName = (map["subjectName"] as? String) ?: "",
            doctorName = (map["doctorName"] as? String) ?: "",
            day = (map["day"] as? String) ?: "",
            time = (map["time"] as? String) ?: "",
            hall = (map["hall"] as? String) ?: "",
            sessions = ((map["sessions"] as? Number)?.toInt()) ?: 2
        )
    }
}

object WeekDays {
    const val SATURDAY = "السبت"
    const val SUNDAY = "الأحد"
    const val MONDAY = "الاثنين"
    const val TUESDAY = "الثلاثاء"
    const val WEDNESDAY = "الأربعاء"
    const val THURSDAY = "الخميس"
    const val FRIDAY = "الجمعة"

    val all = listOf(SATURDAY, SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)

    fun today(): String {
        val cal = java.util.Calendar.getInstance()
        return when (cal.get(java.util.Calendar.DAY_OF_WEEK)) {
            java.util.Calendar.SUNDAY -> SUNDAY
            java.util.Calendar.MONDAY -> MONDAY
            java.util.Calendar.TUESDAY -> TUESDAY
            java.util.Calendar.WEDNESDAY -> WEDNESDAY
            java.util.Calendar.THURSDAY -> THURSDAY
            java.util.Calendar.FRIDAY -> FRIDAY
            else -> SATURDAY
        }
    }

    fun nextOf(day: String): String {
        val idx = all.indexOf(day)
        return if (idx >= 0) all[(idx + 1) % all.size] else all[0]
    }
}