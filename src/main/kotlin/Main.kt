import time.RealClock

fun main() {
    val stats = EventStatisticImpl(RealClock())

    stats.incEvent("Start")
    stats.incEvent("Line")
    stats.incEvent("Line")

    stats.printStatistic()
}
