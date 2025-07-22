package com.example.liontalk.features.chatroomlist

import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.liontalk.ui.theme.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomListScreen(navController: NavHostController) {
    var newRoomName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val viewModel = remember {
        ChatRoomListViewModel(context.applicationContext as Application)
    }
    val state by viewModel.state.observeAsState(ChatRoomListState())

    Scaffold(
        topBar = {
//            TopAppBar(title = { Text("채팅방 목록") })
            CenterAlignedTopAppBar(
                title = {
                    Text( text = "LionTalk",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
//                    IconButton { }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("settings")
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        label = { Text("새 채팅방 이름") },
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (newRoomName.isNotBlank()) {
                                // TODO : 실제 방추가 로직 구현
                                viewModel.createChatRoom(newRoomName)
                            }
                        },
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("방 생성")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (state.isLoading) {
                    CircularProgressIndicator()
                } else if (state.chatRooms.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) { Text("채팅방이 없습니다") }
                } else {
                    LazyColumn {
                        items(state.chatRooms) { room ->
                            ChatRoomItem(
                                room = room,
                                onClick = {
                                    navController.navigate(
                                        Screen.ChatRoomScreen.createRoute(room.id)
                                    )
                                })
                        }
                    }
                }
            }
        }
    )
}
