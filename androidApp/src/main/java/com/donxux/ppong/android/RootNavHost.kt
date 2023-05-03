package com.donxux.ppong.android

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun RootNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "camera",
        builder = getRootNavGraphBuilder(navController)
    )
}

fun getRootNavGraphBuilder(navController: NavHostController): NavGraphBuilder.() -> Unit = {
    composable("camera") {
        CameraScreen(navController)
    }
}
