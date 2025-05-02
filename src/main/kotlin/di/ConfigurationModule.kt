package di

import org.koin.dsl.module

val configurationModule = module {
    single {
        ConfigurationLoader.currentConfig
    }
}