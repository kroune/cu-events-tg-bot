import org.jetbrains.exposed.sql.Database
import org.koin.dsl.module

val databaseModule = module {
    single {
        val config = get<ConfigurationLoader.ConfigMember>()
        val userDataConfig = config.databasesConfig.userData
        Database.connect(
            url = userDataConfig.url,
            driver = "org.postgresql.Driver",
            user = userDataConfig.username,
            password = userDataConfig.password
        )
    }
}