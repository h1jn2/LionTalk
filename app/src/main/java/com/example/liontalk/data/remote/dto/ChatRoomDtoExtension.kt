package com.example.liontalk.data.remote.dto

import com.example.liontalk.model.ChatUser

fun ChatRoomDto.addUserIfNotExists(user: ChatUser): ChatRoomDto {
    val updateUsers = this.users.toMutableList().apply {
        if(none{it.name == user.name}) add(user)
    }
    return this.copy(users = updateUsers)
}

fun ChatRoomDto.removeUser(user: ChatUser): ChatRoomDto {
    val updatedUser = this.users.filterNot { it.name == user.name }
    return this.copy(users = updatedUser)
}