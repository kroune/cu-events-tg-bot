package services

import controllers.EventsTextBuilderController
import data.local.events.EventsRepositoryImpl
import data.local.users.UsersRepositoryImpl
import data.local.usersEvents.UsersEventsRepositoryImpl
import data.remote.alerts.AlertsRemoteRepository
import data.remote.events.Event
import data.remote.events.EventsRemoteRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import logger
import org.koin.core.component.KoinComponent
import retryable

class EventNotificationsService(
    private val eventsRepository: EventsRepositoryImpl,
    private val usersEventsRepository: UsersEventsRepositoryImpl,
    private val eventsRemoteRepository: EventsRemoteRepository,
    private val usersRepository: UsersRepositoryImpl,
    private val alertsRemoteRepository: AlertsRemoteRepository,
    private val eventsTextBuilderController: EventsTextBuilderController
) : KoinComponent {
    private val errorLogger = CoroutineExceptionHandler { context, exception ->
        logger.error(throwable = exception) { "произошла ошибка" }
    }

    private val failSaveScope = CoroutineScope(Dispatchers.IO + SupervisorJob() + errorLogger)

    suspend fun getUsersWithEnabledNotifications(): List<Long>? {
        return retryable({ usersRepository.usersWithEnabledNotifications(failSaveScope.coroutineContext) }) { error ->
            logger.warn(error) { "unable to get users with enabled notifications" }
        }.getOrElse { error ->
            logger.error(error) { "unable to get users with enabled notifications after retries" }
            null
        }
    }

    suspend fun getPastUserEvents(userId: Long): List<Long>? {
        return retryable(
            {
                usersEventsRepository.getEventsByUserId(
                    userId
                )
            }
        ) { exception ->
            logger.warn(exception) { "unable to retrieve user events for $userId" }
        }.getOrElse { exception ->
            logger.error(exception) { "unable to retrieve user events after retries $userId" }
            null
        }
    }

    @Volatile
    private var job: Job? = null
    private val jobLock = Mutex()

    fun start(scope: CoroutineScope) {
        scope.launch {
            jobLock.withLock {
                job?.cancel()
                job = launch {
                    listenForEvent()
                }
            }
        }
    }

    private suspend fun listenForEvent() {
        eventsRemoteRepository.listenForEvents().collect {
            val currentEvents = it.items
            val usersWithEnabledNotifications = getUsersWithEnabledNotifications() ?: return@collect
            usersWithEnabledNotifications.forEach { userId ->
                // async sending to make it faster
                processEventForUser(currentEvents, userId)
            }
        }
    }

    private fun processEventForUser(currentEvents: List<Event>, userId: Long) {
        failSaveScope.launch {
            val pastUserEvents = getPastUserEvents(userId) ?: return@launch
            val newEvents = currentEvents.filter { event -> event.id !in pastUserEvents }
            if (newEvents.isEmpty())
                return@launch
            val messages = buildList {
                add("Появились новые события")
                newEvents.forEach { event ->
                    add(eventsTextBuilderController.constructEventInfo(event))
                }
            }
            messages.forEach { message ->
                alertsRemoteRepository.alert(
                    userId,
                    message
                )
            }
            newEvents.forEach { event ->
                val eventExists = eventsRepository.getEventByEventSlug(event.slug) != null
                if (!eventExists) {
                    eventsRepository.addEvent(event)
                }
                usersEventsRepository.addEventToUser(userId, event.id)
            }
        }
    }

    fun close(): Job {
        return failSaveScope.launch {
            jobLock.withLock {
                job?.cancel()
            }
        }
    }
}