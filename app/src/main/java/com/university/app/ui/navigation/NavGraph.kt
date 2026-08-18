package com.university.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.university.app.data.model.Role
import com.university.app.ui.admin.AdminPanelScreen
import com.university.app.ui.admin.LiveAttendanceScreen
import com.university.app.ui.admin.ManageGradesScreen
import com.university.app.ui.admin.ManageSchedulesScreen
import com.university.app.ui.admin.ManageStudentsScreen
import com.university.app.ui.login.LoginScreen
import com.university.app.ui.settings.SettingsScreen
import com.university.app.ui.student.StudentHomeScreen

object Routes {
    const val LOGIN = "login"
    const val STUDENT = "student"
    const val SETTINGS = "settings"
    const val ADMIN = "admin"
    const val ADMIN_STUDENTS = "admin_students"
    const val ADMIN_SCHEDULES = "admin_schedules"
    const val ADMIN_GRADES = "admin_grades"
    const val ADMIN_ATTENDANCE = "admin_attendance"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    fun goRoot(route: String) {
        navController.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(onLogin = { user ->
                goRoot(if (user.role == Role.ADMIN) Routes.ADMIN else Routes.STUDENT)
            })
        }
        composable(Routes.STUDENT) {
            StudentHomeScreen(
                onLogout = { goRoot(Routes.LOGIN) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = { goRoot(Routes.LOGIN) },
                onAdminOpened = { goRoot(Routes.ADMIN) }
            )
        }
        composable(Routes.ADMIN) {
            AdminPanelScreen(
                onBack = { navController.popBackStack() },
                onStudents = { navController.navigate(Routes.ADMIN_STUDENTS) },
                onSchedules = { navController.navigate(Routes.ADMIN_SCHEDULES) },
                onGrades = { navController.navigate(Routes.ADMIN_GRADES) },
                onAttendance = { navController.navigate(Routes.ADMIN_ATTENDANCE) }
            )
        }
        composable(Routes.ADMIN_STUDENTS) {
            ManageStudentsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ADMIN_SCHEDULES) {
            ManageSchedulesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ADMIN_GRADES) {
            ManageGradesScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.ADMIN_ATTENDANCE) {
            LiveAttendanceScreen(onBack = { navController.popBackStack() })
        }
    }
}