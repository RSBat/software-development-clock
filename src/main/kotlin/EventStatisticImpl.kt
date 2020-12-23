import time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

class EventStatisticImpl(private val clock: Clock) : EventStatistic {
    private val eventQueues: MutableMap<String, MutableList<Instant>> = mutableMapOf()

    override fun incEvent(name: String) {
        val now = clock.now()

        eventQueues.getOrPut(name, { mutableListOf() }).add(now)
    }

    override fun getEventStatisticByName(name: String): Double {
        val now = clock.now()
        shrink(now)

        val queue = eventQueues.getOrDefault(name, mutableListOf())
        return getStatistic(queue)
    }

    override fun getAllEventStatistic(): Map<String, Double> {
        val now = clock.now()
        shrink(now)

        return eventQueues.mapValues { getStatistic(it.value) }
    }

    override fun printStatistic() {
        val allEventStatistic = getAllEventStatistic()

        println("Event\tRPM")
        for ((name, rpm) in allEventStatistic) {
            println("$name\t$rpm")
        }
    }

    private fun shrink(now: Instant) {
        val hourAgo = now.minus(1, ChronoUnit.HOURS)
        val iterator = eventQueues.iterator()

        while (iterator.hasNext()) {
            val queue = iterator.next().value
            queue.removeIf { it.isBefore(hourAgo) }
            if (queue.isEmpty()) {
                iterator.remove()
            }
        }
    }

    private fun getStatistic(queue: List<Instant>): Double {
        return queue.size.toDouble() / MINUTES_IN_HOUR
    }

    private companion object {
        private const val MINUTES_IN_HOUR = 60
    }
}
