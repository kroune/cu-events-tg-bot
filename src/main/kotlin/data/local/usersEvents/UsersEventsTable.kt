package data.local.usersEvents

import data.local.events.EventsTable
import org.jetbrains.exposed.sql.Table

object UsersEventsTable: Table("cu_alert_users_events") {
    val userId = long("user_id").index()
    val eventId = reference("event_id", EventsTable.eventId)
}