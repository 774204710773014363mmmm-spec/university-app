package com.university.app.ui.components

val LEVEL_LABELS = listOf("المستوى الأول", "المستوى الثاني", "المستوى الثالث", "المستوى الرابع")
val TERM_LABELS = listOf("الترم الأول", "الترم الثاني")

fun levelNumber(label: String): Int = when {
    label.contains("الرابع") -> 4
    label.contains("الثالث") -> 3
    label.contains("الثاني") -> 2
    else -> 1
}

fun termNumber(label: String): Int = if (label.contains("الثاني")) 2 else 1