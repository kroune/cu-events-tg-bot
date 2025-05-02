package data.local.events

import data.remote.events.Event
import data.remote.events.EventTicketRegistration
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class EventsRepositoryImpl(
    private val database: Database
) {
    init {
        transaction(database) {
            SchemaUtils.create(EventsTable)
        }
    }

    suspend fun getEventByEventId(id: Long): Event? {
        return newSuspendedTransaction(db = database) {
            EventsTable.selectAll().where { EventsTable.eventId eq id }.map {
                Event(
                    id = it[EventsTable.eventId],
                    slug = it[EventsTable.slug],
                    summary = it[EventsTable.summary],
                    startDate = it[EventsTable.startDate],
                    endDate = it[EventsTable.endDate],
                    createdAt = it[EventsTable.createdAt],
                    shortTitle = it[EventsTable.shortTitle],
                    eventTicketRegistration = EventTicketRegistration(
                        it[EventsTable.isPossibleToRegister],
                        it[EventsTable.status],
                    ),
                    offlineTicketLimit = it[EventsTable.offlineTicketLimit]
                )
            }.singleOrNull()
        }
    }

    suspend fun addEvent(event: Event) {
        newSuspendedTransaction(db = database) {
            EventsTable.insert {
                it[EventsTable.eventId] = event.id
                it[EventsTable.slug] = event.slug
                it[EventsTable.summary] = event.summary
                it[EventsTable.startDate] = event.startDate
                it[EventsTable.endDate] = event.endDate
                it[EventsTable.createdAt] = event.createdAt
                it[EventsTable.shortTitle] = event.shortTitle
                it[EventsTable.isPossibleToRegister] = event.eventTicketRegistration.isPossibleToRegister
                it[EventsTable.status] = event.eventTicketRegistration.status
                it[EventsTable.offlineTicketLimit] = event.offlineTicketLimit
            }
        }
    }
}