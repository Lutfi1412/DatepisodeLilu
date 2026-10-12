package com.lutfilia.rundown.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.lutfilia.rundown.ui.screens.adddate.AddDateScreen
import com.lutfilia.rundown.ui.screens.detail.DetailScreen
import com.lutfilia.rundown.ui.screens.home.HomeScreen
import com.lutfilia.rundown.ui.screens.rundownform.CreateRundownScreen
import com.lutfilia.rundown.ui.screens.splash.SplashScreen
import com.lutfilia.rundown.ui.screens.update.UpdateDialog
import com.lutfilia.rundown.ui.screens.update.VersionViewModel
import com.lutfilia.rundown.util.openLink

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ADD_DATE = "add_date"
    const val CREATE = "create_rundown/{tanggalId}?date={date}"
    const val DETAIL = "detail/{tanggalId}?fresh={fresh}"

    /** date = ISO "2026-10-11" (boleh kosong). */
    fun create(tanggalId: Int, dateIso: String) = "create_rundown/$tanggalId?date=$dateIso"
    fun detail(tanggalId: Int, fresh: Boolean = false) = "detail/$tanggalId?fresh=$fresh"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    versionViewModel: VersionViewModel = viewModel()
) {
    // Cek versi jalan di latar belakang sejak aplikasi dibuka; tidak menahan splash.
    val updateInfo by versionViewModel.update.collectAsStateWithLifecycle()
    val context = LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { slideInHorizontally(tween(360)) { it / 10 } + fadeIn(tween(320, delayMillis = 100)) },
        exitTransition = { fadeOut(tween(160)) },
        popEnterTransition = { fadeIn(tween(300, delayMillis = 80)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it / 10 } + fadeOut(tween(220)) }
    ) {
        composable(
            Routes.SPLASH,
            enterTransition = { fadeIn(tween(300)) },
            exitTransition = { fadeOut(tween(500)) }
        ) {
            SplashScreen(
                updateRequired = updateInfo != null,
                onFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onAddDate = { navController.navigate(Routes.ADD_DATE) },
                onOpenDate = { navController.navigate(Routes.detail(it.id)) }
            )
        }

        composable(Routes.ADD_DATE) {
            AddDateScreen(
                onBack = { navController.popBackStack() },
                onCreated = { id, date ->
                    navController.navigate(Routes.create(id, date.toString())) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            Routes.CREATE,
            arguments = listOf(
                navArgument("tanggalId") { type = NavType.IntType },
                navArgument("date") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            CreateRundownScreen(
                onBack = { navController.popBackStack() },
                onFinished = { id ->
                    navController.navigate(Routes.detail(id, fresh = true)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        composable(
            Routes.DETAIL,
            arguments = listOf(
                navArgument("tanggalId") { type = NavType.IntType },
                navArgument("fresh") { type = NavType.BoolType; defaultValue = false }
            )
        ) {
            DetailScreen(
                onBack = { navController.popBackStack() },
                onAddRundown = { id, dateIso -> navController.navigate(Routes.create(id, dateIso)) }
            )
        }
    }

    // Popup tampil di atas layar mana pun (splash maupun home) dan tetap ada sampai aplikasi diperbarui.
    updateInfo?.let { info ->
        UpdateDialog(
            info = info,
            onConfirm = {
                if (info.link.isNotBlank()) context.openLink(info.link)
                else versionViewModel.dismiss() // tidak ada link dari server: jangan kunci pengguna
            }
        )
    }
}