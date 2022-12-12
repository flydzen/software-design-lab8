package events

import clock.SetableClock
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.temporal.ChronoUnit

const val MINUTES_PER_HOUR = 60.0

class EventStatisticImplTest {
    companion object {
        private lateinit var eventsStatistic: EventsStatistic
        private lateinit var clock: SetableClock
    }

    private fun addMinutes(amount: Long = 1) {
        clock.setNow(clock.now().plus(amount, ChronoUnit.MINUTES))
    }

    @BeforeEach
    fun setUp() {
        val instant = Instant.now()
        clock = SetableClock(instant)
        eventsStatistic = EventStatisticImpl(clock)
    }

    @Test
    fun testEmpty() {
        assertEquals(eventsStatistic.getAllEventStatistic(), mapOf<String, Double>())
    }

    @Test
    fun testNonExistentEvent() {
        assertEquals(0.0, eventsStatistic.getEventStatisticByName("a"))
    }

    @Test
    fun testIncEventOneHour() {
        repeat(30) { eventsStatistic.incEvent("a") }
        addMinutes(5)
        repeat(30) { eventsStatistic.incEvent("a") }
        assertEquals(1.0, eventsStatistic.getEventStatisticByName("a"))
    }

    @Test
    fun testIncEventManyHours() {
        repeat(MINUTES_PER_HOUR.toInt() * 3) {
            eventsStatistic.incEvent("a")
            addMinutes()
        }
        assertEquals(1.0, eventsStatistic.getEventStatisticByName("a"))
        addMinutes()
        assertEquals(59.0 / MINUTES_PER_HOUR, eventsStatistic.getEventStatisticByName("a"))
    }

    @Test
    fun testGetAllEventStatistic() {
        // a: <0 | 1 2 ...>
        // b: <0 | 1   ...>
        // expected number of events: |a| = 2, |b| = 1

        repeat(2) {
            eventsStatistic.incEvent("a")
            eventsStatistic.incEvent("b")
            addMinutes(1)
        }
        eventsStatistic.incEvent("a")

        addMinutes(58)
        clock.setNow(clock.now().plus(1, ChronoUnit.SECONDS))

        assertEquals(
            mapOf("a" to 2 / MINUTES_PER_HOUR, "b" to 1 / MINUTES_PER_HOUR),
            eventsStatistic.getAllEventStatistic()
        )
    }

    @Test
    fun testPrintStatistic() {
        repeat(3) {
            eventsStatistic.incEvent("a")
            eventsStatistic.incEvent("b")
            addMinutes(1)
        }

        val baos = ByteArrayOutputStream()
        val charset = StandardCharsets.UTF_8.name()
        val printStream = PrintStream(baos, true, charset)
        System.setOut(printStream)

        eventsStatistic.printStatistic()

        assertEquals(
            "{a=0.05, b=0.05}${System.lineSeparator()}",
            baos.toString(charset),
        )
    }
}