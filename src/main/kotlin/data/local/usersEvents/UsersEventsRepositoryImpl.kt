package data.local.usersEvents

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UsersEventsRepositoryImpl(val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(UsersEventsTable)
        }
    }

    suspend fun getEventsByUserId(id: Long): List<Long> {
        return newSuspendedTransaction(db = database) {
            UsersEventsTable.selectAll().where { UsersEventsTable.userId eq id }.map {
                it[UsersEventsTable.eventId]
            }
        }
    }

    suspend fun addEventToUser(
        userId: Long,
        eventId: Long
    ) {
        newSuspendedTransaction(db = database) {
            UsersEventsTable.insert {
                it[UsersEventsTable.userId] = userId
                it[UsersEventsTable.eventId] = eventId
            }
        }
    }
}