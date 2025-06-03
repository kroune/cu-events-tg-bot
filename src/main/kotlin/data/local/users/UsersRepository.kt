package data.local.users

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.coroutines.CoroutineContext

class UsersRepositoryImpl(val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(UsersTable)
        }
    }

    suspend fun usersWithEnabledNotifications(coroutineContext: CoroutineContext? = null): Result<List<Long>> {
        return runCatching {
            newSuspendedTransaction(coroutineContext, db = database) {
                UsersTable.selectAll().where { UsersTable.shouldNotify eq true }.map {
                    it[UsersTable.userId]
                }
            }
        }
    }

    suspend fun enableNotifications(
        userId: Long,
    ) {
        newSuspendedTransaction(db = database) {
            UsersTable.update({ UsersTable.userId eq userId }) {
                it[UsersTable.shouldNotify] = true
            }
        }
    }

    suspend fun disableNotifications(
        userId: Long,
    ) {
        newSuspendedTransaction(db = database) {
            UsersTable.update({ UsersTable.userId eq userId }) {
                it[UsersTable.shouldNotify] = false
            }
        }
    }

    suspend fun createUser(
        userId: Long,
    ) {
        newSuspendedTransaction(db = database) {
            UsersTable.insert {
                it[UsersTable.userId] = userId
                it[UsersTable.shouldNotify] = false
            }
        }
    }

    suspend fun isRegistered(userId: Long): Boolean {
        return newSuspendedTransaction(db = database) {
            UsersTable.selectAll().where { UsersTable.userId eq userId }.any()
        }
    }
}