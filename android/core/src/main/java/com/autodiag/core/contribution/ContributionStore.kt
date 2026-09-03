package com.autodiag.core.contribution

interface ContributionStore {
    fun enqueue(record: ContributionRecord)
    fun pending(): List<ContributionRecord>
    fun clear()
}

class InMemoryContributionStore : ContributionStore {
    private val records = mutableListOf<ContributionRecord>()

    @Synchronized
    override fun enqueue(record: ContributionRecord) {
        records.add(record)
    }

    @Synchronized
    override fun pending(): List<ContributionRecord> = records.toList()

    @Synchronized
    override fun clear() {
        records.clear()
    }
}
