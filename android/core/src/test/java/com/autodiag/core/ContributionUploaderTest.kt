package com.autodiag.core

import com.autodiag.core.contribution.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ContributionUploaderTest {
    private fun sampleRecord(id: String) = ContributionRecord(id, 1, 1, null, null, null, "2026-09", emptyList(), emptyList(), "0.1-dev")

    private class RecordingTransport(private val fail: Boolean = false) : ContributionTransport {
        var lastBatch: List<ContributionRecord>? = null
        override suspend fun upload(batch: List<ContributionRecord>): Result<Unit> {
            lastBatch = batch
            return if (fail) Result.failure(IllegalStateException("network down")) else Result.success(Unit)
        }
    }

    @Test fun neverUploadsWithoutConsentEvenIfEndpointConfigured() = runBlocking {
        val config = ContributionConfig(endpointUrl = "https://ingest.example.org/v1/contributions")
        val store = InMemoryContributionStore().apply { enqueue(sampleRecord("a")) }
        val transport = RecordingTransport()
        val consent = ContributionConsentManager(InMemoryConsentStore(), store, config)
        val result = ContributionUploader(consent, store, transport, config).flush()
        assertEquals(ContributionUploadResult.SkippedNoConsent, result)
        assertNull(transport.lastBatch)
        assertTrue(store.pending().isNotEmpty())
    }

    @Test fun neverUploadsWhenEndpointNotConfiguredEvenWithConsent() = runBlocking {
        val config = ContributionConfig(endpointUrl = null)
        val store = InMemoryContributionStore().apply { enqueue(sampleRecord("a")) }
        val transport = RecordingTransport()
        val consent = ContributionConsentManager(InMemoryConsentStore(), store, config)
        consent.grant(1L)
        val result = ContributionUploader(consent, store, transport, config).flush()
        assertEquals(ContributionUploadResult.SkippedDisabled, result)
        assertNull(transport.lastBatch)
    }

    @Test fun uploadsAndClearsQueueOnSuccessWithConsent() = runBlocking {
        val config = ContributionConfig(endpointUrl = "https://ingest.example.org/v1/contributions")
        val store = InMemoryContributionStore().apply { enqueue(sampleRecord("a")); enqueue(sampleRecord("b")) }
        val transport = RecordingTransport()
        val consent = ContributionConsentManager(InMemoryConsentStore(), store, config)
        consent.grant(1L)
        val result = ContributionUploader(consent, store, transport, config).flush()
        assertEquals(ContributionUploadResult.Uploaded(2), result)
        assertEquals(2, transport.lastBatch!!.size)
        assertTrue(store.pending().isEmpty())
    }

    @Test fun keepsQueueOnTransportFailure() = runBlocking {
        val config = ContributionConfig(endpointUrl = "https://ingest.example.org/v1/contributions")
        val store = InMemoryContributionStore().apply { enqueue(sampleRecord("a")) }
        val transport = RecordingTransport(fail = true)
        val consent = ContributionConsentManager(InMemoryConsentStore(), store, config)
        consent.grant(1L)
        val result = ContributionUploader(consent, store, transport, config).flush()
        assertTrue(result is ContributionUploadResult.Failed)
        assertTrue(store.pending().isNotEmpty())
    }
}
