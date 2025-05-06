import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

object ConfigurationLoader {
    @Serializable
    data class ConfigMember(
        val serverConfig: ServerConfig,
        val prometheusScraperConfig: PrometheusScraperConfig,
        val botToken: String,
        val authCookie: String,
        val databasesConfig: DatabasesConfig
    )


    @Serializable
    class ServerConfig(
        val host: String,
        val port: Int,
    )

    @Serializable
    class DatabasesConfig(
        val userData: PostgresConfig
    )

    @Serializable
    class PrometheusScraperConfig(
        val nameForScrape: String,
        val passwordForScrape: String,
    )

    @Serializable
    class PostgresConfig(
        val url: String,
        val username: String,
        val password: String
    )

    private fun loadConfig(): ConfigMember {
        val configDirectory = System.getenv("CONFIG_PATH") ?: "/etc/cu-events-bot/config.json"
        val config = File(configDirectory).readText()
        return Json.decodeFromString<ConfigMember>(config)
    }

    val currentConfig: ConfigMember = loadConfig()
}