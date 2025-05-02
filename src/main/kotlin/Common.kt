import io.github.oshai.kotlinlogging.KLogger
import io.ktor.server.application.Application
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.mp.KoinPlatformTools

inline fun <reified T : Any> inject(
    qualifier: Qualifier? = null,
    mode: LazyThreadSafetyMode = KoinPlatformTools.defaultLazyMode(),
    noinline parameters: ParametersDefinition? = null,
): Lazy<T> {
    return GlobalContext.get().inject<T>(
        qualifier,
        mode,
        parameters
    )
}

inline fun <reified T : Any> get(
    qualifier: Qualifier? = null,
    noinline parameters: ParametersDefinition? = null,
): T {
    return GlobalContext.get().get<T>(
        qualifier,
        parameters
    )
}

val globalLogger: KLogger
    get() = GlobalContext.get().inject<KLogger>().value

val Routing.logger: KLogger
    get() = inject<KLogger>().value

val Route.logger: KLogger
    get() = inject<KLogger>().value

val Application.logger: KLogger
    get() = inject<KLogger>().value