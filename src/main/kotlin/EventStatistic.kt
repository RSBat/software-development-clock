interface EventStatistic {
    fun incEvent(name: String)
    fun getEventStatisticByName(name: String): Double
    fun getAllEventStatistic(): Map<String, Double>
    fun printStatistic()
}
