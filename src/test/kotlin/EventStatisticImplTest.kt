import java.time.Instant
import java.time.temporal.ChronoUnit
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import time.Clock

internal class EventStatisticImplTest {
    private val testClock = TestClock()
    private val eventStatistic = EventStatisticImpl(testClock)

    @Test
    fun `when no event is recorded rpm is zero`() {
        assertEquals(0.0, eventStatistic.getEventStatisticByName(TEST_EVENT))
    }

    @Test
    fun `record event once`() {
        eventStatistic.incEvent(TEST_EVENT)
        assertEquals(1.0 / 60, eventStatistic.getEventStatisticByName(TEST_EVENT))
    }

    @Test
    fun `record event multiple times`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
            TEST_EVENT to Instant.EPOCH.plus(1, ChronoUnit.MINUTES),
            TEST_EVENT to Instant.EPOCH.plus(5, ChronoUnit.MINUTES),
            TEST_EVENT to Instant.EPOCH.plus(10, ChronoUnit.MINUTES),
        ))

        assertEquals(4.0 / 60, eventStatistic.getEventStatisticByName(TEST_EVENT))
    }

    @Test
    fun `different events do not affect each other`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
            OTHER_EVENT to Instant.EPOCH.plus(1, ChronoUnit.MINUTES),
            OTHER_EVENT to Instant.EPOCH.plus(5, ChronoUnit.MINUTES),
        ))

        assertEquals(1.0 / 60, eventStatistic.getEventStatisticByName(TEST_EVENT))
        assertEquals(2.0 / 60, eventStatistic.getEventStatisticByName(OTHER_EVENT))
    }

    @Test
    fun `old events are not counted`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
            TEST_EVENT to Instant.EPOCH.plus(1, ChronoUnit.MINUTES),
            TEST_EVENT to Instant.EPOCH.plus(70, ChronoUnit.MINUTES),
            TEST_EVENT to Instant.EPOCH.plus(80, ChronoUnit.MINUTES),
        ))

        assertEquals(2.0 / 60, eventStatistic.getEventStatisticByName(TEST_EVENT))
    }

    @Test
    fun `old events at call time are not counted`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
            TEST_EVENT to Instant.EPOCH.plus(30, ChronoUnit.MINUTES),
        ))

        testClock.instant = Instant.EPOCH.plus(70, ChronoUnit.MINUTES)
        assertEquals(1.0 / 60, eventStatistic.getEventStatisticByName(TEST_EVENT))
    }

    @Test
    fun `when no event is recorded stat is empty`() {
        assertTrue(eventStatistic.getAllEventStatistic().isEmpty())
    }

    @Test
    fun `when single event recorded stats contain one event`() {
        eventStatistic.incEvent(TEST_EVENT)

        val stats = eventStatistic.getAllEventStatistic()
        assertEquals(1, stats.size)
        assertEquals(1.0 / 60, stats[TEST_EVENT])
    }

    @Test
    fun `when multiple events recorded stats contain all events`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
            OTHER_EVENT to Instant.EPOCH.plus(1, ChronoUnit.MINUTES),
            OTHER_EVENT to Instant.EPOCH.plus(5, ChronoUnit.MINUTES),
        ))

        val stats = eventStatistic.getAllEventStatistic()
        assertEquals(2, stats.size)
        assertEquals(1.0 / 60, stats[TEST_EVENT])
        assertEquals(2.0 / 60, stats[OTHER_EVENT])
    }

    @Test
    fun `old events are not in stats`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
            TEST_EVENT to Instant.EPOCH.plus(1, ChronoUnit.MINUTES),
            TEST_EVENT to Instant.EPOCH.plus(70, ChronoUnit.MINUTES),
            TEST_EVENT to Instant.EPOCH.plus(80, ChronoUnit.MINUTES),
        ))

        val stats = eventStatistic.getAllEventStatistic()
        assertEquals(1, stats.size)
        assertEquals(2.0 / 60, stats[TEST_EVENT])
    }

    @Test
    fun `old events at call time are not in stats`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
            TEST_EVENT to Instant.EPOCH.plus(30, ChronoUnit.MINUTES),
        ))

        testClock.instant = Instant.EPOCH.plus(70, ChronoUnit.MINUTES)
        val stats = eventStatistic.getAllEventStatistic()
        assertEquals(1, stats.size)
        assertEquals(1.0 / 60, stats[TEST_EVENT])
    }

    @Test
    fun `events with zero rpm are not in stats`() {
        incAtTimestamps(listOf(
            TEST_EVENT to Instant.EPOCH,
        ))

        testClock.instant = Instant.EPOCH.plus(70, ChronoUnit.MINUTES)
        val stats = eventStatistic.getAllEventStatistic()
        assertTrue(stats.isEmpty())
    }

    private fun incAtTimestamp(name: String, instant: Instant) {
        testClock.instant = instant
        eventStatistic.incEvent(name)
    }

    private fun incAtTimestamps(events: List<Pair<String, Instant>>) {
        events.forEach { (name, instant) -> incAtTimestamp(name, instant) }
    }

    class TestClock: Clock {
        var instant: Instant = Instant.EPOCH

        override fun now() = instant
    }

    companion object {
        const val TEST_EVENT = "test_event"
        const val OTHER_EVENT = "other_event"
    }
}
