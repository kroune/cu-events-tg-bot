package controllers

import data.local.users.UsersRepositoryImpl
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class UserController : KoinComponent {
    private val usersRepository by inject<UsersRepositoryImpl>()

    suspend fun registerUser(userId: Long) {
        if (usersRepository.isRegistered(userId))
            return
        usersRepository.createUser(userId)
    }

    suspend fun enableNotifications(userId: Long) {
        usersRepository.enableNotifications(userId)
    }

    suspend fun disableNotifications(userId: Long) {
        usersRepository.disableNotifications(userId)
    }
}