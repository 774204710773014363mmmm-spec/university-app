package com.university.app.util

import android.content.Context
import com.university.app.data.model.User

object SessionManager {

    private const val PREFS = "session"
    private const val KEY_USER_ID = "userId"
    private const val KEY_NAME = "name"
    private const val KEY_ROLE = "role"

    data class Session(val userId: String, val name: String, val role: String)

    fun save(context: Context, user: User) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_NAME, user.name)
            .putString(KEY_ROLE, user.role)
            .apply()
    }

    fun current(context: Context): Session? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        return Session(
            userId = id,
            name = prefs.getString(KEY_NAME, "") ?: "",
            role = prefs.getString(KEY_ROLE, "") ?: ""
        )
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}