package com.example.liontalk.ui.theme.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.liontalk.features.chatroom.ChatRoomScreen
import com.example.liontalk.features.chatroomlist.ChatRoomListScreen
import com.example.liontalk.features.launcher.LauncherScreen
import com.example.liontalk.features.setting.SettingScreen

@Composable
fun ChatAppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.LauncherScreen.route
    ) {
        composable(Screen.ChatRoomListScreen.route) {
            ChatRoomListScreen(navController)
        }
        composable(Screen.ChatRoomScreen.route) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId")?.toIntOrNull()
            if (roomId != null) {
                ChatRoomScreen(roomId)
            }
        }
        composable(Screen.SettingScreen.route) {
            SettingScreen(navController)
        }
        composable(Screen.LauncherScreen.route) {
            LauncherScreen(navController)
        }
    }
}
