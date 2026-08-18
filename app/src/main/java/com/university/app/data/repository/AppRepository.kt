package com.university.app.data.repository

import com.university.app.data.model.Attendance
import com.university.app.data.model.Grade
import com.university.app.data.model.Schedule
import com.university.app.data.model.ScheduleRule
import com.university.app.data.model.User

interface AppRepository {
    suspend fun login(identifier: String, password: String): User?

    suspend fun getUser(userId: String): User?

    suspend fun checkAdminPassword(password: String): Boolean

    suspend fun addStudent(user: User)

    suspend fun deleteStudent(studentId: String)

    suspend fun getStudents(): List<User>

    suspend fun getStudentsByLevel(level: Int): List<User>

    suspend fun getSchedule(level: Int, term: Int, day: String): List<Schedule>

    suspend fun getAllSchedules(): List<Schedule>

    suspend fun addSchedule(schedule: Schedule)

    suspend fun deleteSchedule(id: String)

    suspend fun clearSchedules()

    suspend fun generateAutoSchedule(): Int

    suspend fun getSubjects(level: Int, term: Int): List<String>

    suspend fun getGrades(studentId: String, level: Int, term: Int): List<Grade>

    suspend fun importGrades(records: List<Grade>): Int

    suspend fun getAttendance(studentId: String, level: Int, term: Int, subject: String): List<Attendance>

    suspend fun getLastLecture(level: Int, term: Int, subject: String): Int

    suspend fun markAttendance(record: Attendance)

    suspend fun importAttendance(records: List<Attendance>): Int
}