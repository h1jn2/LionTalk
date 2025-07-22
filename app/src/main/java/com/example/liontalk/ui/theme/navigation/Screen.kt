package com.example.liontalk.ui.theme.navigation

sealed class Screen(val route: String) {
    object ChatRoomListScreen: Screen("chatroom_list")
    object ChatRoomScreen: Screen("chatroom_detail/{roomId}") {
        fun createRoute(roomId: Int) = "chatroom_detail/$roomId"
    }
    object SettingScreen: Screen("settings")
    object LauncherScreen: Screen("launcher")
}