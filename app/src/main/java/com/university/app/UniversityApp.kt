package com.university.app

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.university.app.data.repository.AppRepository
import com.university.app.data.repository.DemoRepository
import com.university.app.data.repository.FirestoreRepository

object AppService {
    lateinit var repo: AppRepository
        private set

    fun init(context: Context) {
        val useFirebase = FirebaseApp.getApps(context).isNotEmpty()
        repo = if (useFirebase) {
            FirestoreRepository()
        } else {
            DemoRepository(context.getSharedPreferences("demo_data", Context.MODE_PRIVATE))
        }
    }
}

class UniversityApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppService.init(this)
    }
}