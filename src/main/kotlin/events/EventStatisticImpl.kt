package events

import clock.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

const val MINUTES_PER_HOUR = 60.0

class EventStatisticImpl(private val clock: Clock): EventsStatistic {
    private val events = mutableMapOf<String, ArrayDeque<Instant>>()

    override fun incEvent(name: String) {
        events.computeIfAbsent(name) { ArrayDeque() }.add(clock.now())
    }

    override fun getEventStatisticByName(name: String): Double {
        refreshQueue(name)
        return (events[name]?.size ?: 0) / MINUTES_PER_HOUR
    }

    override fun getAllEventStatistic() = events.mapValues { getEventStatisticByName(it.key) }

    override fun printStatistic() {
        println(getAllEventStatistic())
    }

    private fun refreshQueue(name: String) {
        events[name]?.let {
            events[name] = ArrayDeque(events[name]!!.dropWhile {
                it.isBefore(clock.now().minus(1, ChronoUnit.HOURS))
            })
        }
    }
}