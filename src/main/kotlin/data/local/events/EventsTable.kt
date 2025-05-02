package data.local.events

import org.jetbrains.exposed.sql.Table

object EventsTable : Table("cu_alert_events") {
    val eventId = long("event_id").uniqueIndex()
    val slug = varchar("slug", 255)
    val summary = varchar("summary", 255).nullable()
    val startDate = varchar("start_date", 255)
    val endDate = varchar("end_date", 255)
    val createdAt = varchar("created_at", 255)
    val shortTitle = varchar("short_title", 255)
    val isPossibleToRegister = bool("is_possible_to_register")
    val status = varchar("status", 255)
    val offlineTicketLimit = long("offline_ticket_limit").nullable()

    override val primaryKey = PrimaryKey(eventId)
}