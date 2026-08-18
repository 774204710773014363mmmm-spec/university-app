package com.university.app.util

import android.content.Context
import android.net.Uri
import com.university.app.data.model.AttendanceStatus
import com.university.app.data.model.Role
import com.university.app.data.model.Schedule
import com.university.app.data.model.User
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory

object ExcelReader {

    data class GradeRow(val studentId: String, val score: Double)

    data class AttendanceRow(val studentId: String, val lectureNumber: Int, val status: String)

    private val dataFormatter = DataFormatter()

    fun readUsers(context: Context, uri: Uri): List<User> {
        val matrix = readCells(context, uri)
        val rows = parse(matrix) { row, h -> buildUser(row, h) }
        return rows
    }

    fun readGrades(context: Context, uri: Uri): List<GradeRow> {
        val matrix = readCells(context, uri)
        return parse(matrix) { row, h -> buildGradeRow(row, h) }
    }

    fun readSchedules(context: Context, uri: Uri): List<Schedule> {
        val matrix = readCells(context, uri)
        return parse(matrix) { row, h -> buildSchedule(row, h) }
    }

    fun readAttendance(context: Context, uri: Uri): List<AttendanceRow> {
        val matrix = readCells(context, uri)
        return parse(matrix) { row, h -> buildAttendanceRow(row, h) }
    }

    private class Row(private val cells: List<String>) {
        val count: Int get() = cells.size
        fun cell(i: Int): String = if (i in cells.indices) cells[i] else ""
    }

    private class HeaderInfo(
        val detected: Boolean,
        val name: Int = -1, val number: Int = -1, val password: Int = -1,
        val phone: Int = -1, val score: Int = -1, val level: Int = -1, val term: Int = -1,
        val day: Int = -1, val subject: Int = -1, val time: Int = -1, val hall: Int = -1,
        val doctor: Int = -1, val lecture: Int = -1, val status: Int = -1
    )

    private fun <T> parse(
        matrix: List<List<String>>?,
        builder: (Row, HeaderInfo) -> T?
    ): List<T> {
        if (matrix.isNullOrEmpty()) return emptyList()
        val header = matrix[0]
        val h = detectHeader(header)
        val startRow = if (h.detected) 1 else 0
        val result = mutableListOf<T>()
        for (r in startRow until matrix.size) {
            val cells = matrix[r]
            if (cells.all { it.isBlank() }) continue
            val row = Row(cells)
            builder(row, h)?.let { result += it }
        }
        return result
    }

    private fun detectHeader(header: List<String>): HeaderInfo {
        fun index(keywords: List<String>): Int {
            for ((i, cell) in header.withIndex()) {
                val text = cell.lowercase()
                if (keywords.any { text.contains(it) }) return i
            }
            return -1
        }
        val name = index(listOf("اسم", "name"))
        val number = index(listOf("رقم", "id", "code", "student"))
        val password = index(listOf("سر", "pass"))
        val phone = index(listOf("جوال", "هاتف", "phone", "mobile"))
        val score = index(listOf("درجة", "score", "grade", "mark"))
        val level = index(listOf("مستوى", "level"))
        val term = index(listOf("ترم", "term"))
        val day = index(listOf("يوم", "day"))
        val subject = index(listOf("مادة", "subject", "course", "course name"))
        val time = index(listOf("وقت", "time"))
        val hall = index(listOf("قاعة", "hall", "room"))
        val doctor = index(listOf("دكتور", "استاذ", "doctor", "أستاذ"))
        val lecture = index(listOf("محاضرة", "lecture"))
        val status = index(listOf("حالة", "status", "حضور", "غياب"))
        val detected = name >= 0 || number >= 0 || password >= 0 || score >= 0 || level >= 0 ||
            subject >= 0 || day >= 0 || time >= 0 || hall >= 0 || doctor >= 0 || lecture >= 0 || status >= 0
        return HeaderInfo(
            detected, name, number, password, phone, score, level, term,
            day, subject, time, hall, doctor, lecture, status
        )
    }

    private fun buildUser(row: Row, h: HeaderInfo): User? {
        val nameIdx = h.name.takeIf { it >= 0 } ?: 0
        val numIdx = h.number.takeIf { it >= 0 } ?: 1
        val pwIdx = h.password.takeIf { it >= 0 } ?: 2
        val phIdx = h.phone.takeIf { it >= 0 } ?: -1
        val number = row.cell(numIdx)
        if (number.isEmpty()) return null
        return User(
            id = number,
            name = row.cell(nameIdx),
            role = Role.STUDENT,
            phone = if (phIdx >= 0) row.cell(phIdx) else "",
            password = if (pwIdx >= 0 && pwIdx < row.count) row.cell(pwIdx).ifEmpty { "123456" } else "123456"
        )
    }

    private fun buildGradeRow(row: Row, h: HeaderInfo): GradeRow? {
        val numIdx = h.number.takeIf { it >= 0 } ?: 0
        val scoreIdx = h.score.takeIf { it >= 0 } ?: 1
        val number = row.cell(numIdx)
        if (number.isEmpty()) return null
        val score = parseDouble(row.cell(scoreIdx)) ?: return null
        return GradeRow(number, score)
    }

    private fun buildSchedule(row: Row, h: HeaderInfo): Schedule? {
        val lvlIdx = h.level.takeIf { it >= 0 } ?: 0
        val termIdx = h.term.takeIf { it >= 0 } ?: 1
        val dayIdx = h.day.takeIf { it >= 0 } ?: 2
        val subjIdx = h.subject.takeIf { it >= 0 } ?: 3
        val timeIdx = h.time.takeIf { it >= 0 } ?: 4
        val hallIdx = h.hall.takeIf { it >= 0 } ?: 5
        val docIdx = h.doctor.takeIf { it >= 0 } ?: 6
        val subject = row.cell(subjIdx)
        if (subject.isEmpty()) return null
        return Schedule(
            level = parseInt(row.cell(lvlIdx)) ?: 1,
            term = parseInt(row.cell(termIdx)) ?: 1,
            day = row.cell(dayIdx),
            subjectName = subject,
            time = row.cell(timeIdx),
            hall = if (hallIdx < row.count) row.cell(hallIdx) else "",
            doctorName = if (docIdx < row.count) row.cell(docIdx) else ""
        )
    }

    private fun buildAttendanceRow(row: Row, h: HeaderInfo): AttendanceRow? {
        val numIdx = h.number.takeIf { it >= 0 } ?: 0
        val lecIdx = h.lecture.takeIf { it >= 0 } ?: 1
        val stIdx = h.status.takeIf { it >= 0 } ?: 2
        val number = row.cell(numIdx)
        if (number.isEmpty()) return null
        val status = if (stIdx < row.count) AttendanceStatus.fromRaw(row.cell(stIdx)) else AttendanceStatus.PRESENT
        return AttendanceRow(
            studentId = number,
            lectureNumber = parseInt(row.cell(lecIdx)) ?: 1,
            status = status
        )
    }

    private fun readCells(context: Context, uri: Uri): List<List<String>>? {
        val fileName = uri.lastPathSegment?.lowercase() ?: ""
        val mime = context.contentResolver.getType(uri) ?: ""
        return if (fileName.endsWith(".csv") || mime.startsWith("text/")) {
            readCsv(context, uri)
        } else {
            readExcel(context, uri)
        }
    }

    private fun readExcel(context: Context, uri: Uri): List<List<String>>? {
        val stream = context.contentResolver.openInputStream(uri) ?: return null
        return stream.use { input ->
            val wb = try {
                WorkbookFactory.create(input)
            } catch (e: Exception) {
                null
            } ?: return@use null
            wb.use {
                val sheet = wb.getSheetAt(0) ?: return@use null
                val matrix = mutableListOf<List<String>>()
                for (r in 0..sheet.lastRowNum) {
                    val row = sheet.getRow(r) ?: continue
                    val cells = mutableListOf<String>()
                    for (c in 0 until row.lastCellNum.toInt()) {
                        val cell = row.getCell(c)
                        cells += if (cell == null) "" else normalizeDigits(dataFormatter.formatCellValue(cell)).trim()
                    }
                    if (cells.isNotEmpty()) matrix += cells
                }
                matrix
            }
        }
    }

    private fun readCsv(context: Context, uri: Uri): List<List<String>>? {
        val stream = context.contentResolver.openInputStream(uri) ?: return null
        val text = stream.use { it.readBytes().toString(Charsets.UTF_8) }
        val lines = text.replace("\r", "").split("\n")
        return lines
            .filter { it.isNotBlank() }
            .map { line -> line.split(",").map { normalizeDigits(it.trim()) } }
    }

    private fun normalizeDigits(input: String): String =
        input
            .replace('٠', '0').replace('١', '1').replace('٢', '2').replace('٣', '3').replace('٤', '4')
            .replace('٥', '5').replace('٦', '6').replace('٧', '7').replace('٨', '8').replace('٩', '9')
            .replace('۰', '0').replace('۱', '1').replace('۲', '2').replace('۳', '3').replace('۴', '4')
            .replace('۵', '5').replace('۶', '6').replace('۷', '7').replace('۸', '8').replace('۹', '9')
            .replace('٫', '.')

    private fun parseInt(text: String): Int? =
        normalizeDigits(text).toIntOrNull()

    private fun parseDouble(text: String): Double? {
        val normalized = normalizeDigits(text).replace(",", ".")
        return normalized.toDoubleOrNull()
    }
}