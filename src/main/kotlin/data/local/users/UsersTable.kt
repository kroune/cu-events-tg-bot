package data.local.users

import org.jetbrains.exposed.sql.Table

object UsersTable : Table("cu_alert_users") {
    val userId = long("user_id").uniqueIndex()
    val shouldNotify = bool("should_notify")

    override val primaryKey: PrimaryKey = PrimaryKey(userId)
}