package ru.marwinka.androidarchsample.core.network

import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RateLimitInterceptorTest {
    private val server = MockWebServer()

    /** Virtual time: sleeping advances the clock, nothing waits for real. */
    private var now = 0L
    private val sleeps = mutableListOf<Long>()

    private fun client(maxRetries: Int = 5) =
        OkHttpClient
            .Builder()
            .addInterceptor(
                RateLimitInterceptor(
                    minIntervalMillis = 300,
                    maxRetries = maxRetries,
                    backoffMillis = 1_000,
                    clock = { now },
                    sleep = {
                        sleeps += it
                        now += it
                    },
                ),
            ).build()

    private fun get(client: OkHttpClient) = client.newCall(Request.Builder().url(server.url("/")).build()).execute()

    private fun tooManyRequests(retryAfter: String? = null) =
        MockResponse
            .Builder()
            .code(429)
            .apply { if (retryAfter != null) addHeader("Retry-After", retryAfter) }
            .build()

    private fun ok() =
        MockResponse
            .Builder()
            .code(200)
            .body("ok")
            .build()

    @Before
    fun setUp() {
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `consecutive requests are spaced by the minimal interval`() {
        repeat(3) { server.enqueue(ok()) }
        val client = client()

        repeat(3) { get(client).close() }

        assertEquals(listOf(300L, 300L), sleeps)
    }

    @Test
    fun `waits for Retry-After and then succeeds`() {
        server.enqueue(tooManyRequests(retryAfter = "10"))
        server.enqueue(ok())

        get(client()).use { response -> assertEquals(200, response.code) }

        assertEquals(listOf(10_000L), sleeps)
    }

    @Test
    fun `Retry-After 0 falls back to exponential backoff`() {
        // Cloudflare keeps answering 429 with Retry-After: 0 until the block ends.
        server.enqueue(tooManyRequests(retryAfter = "10"))
        server.enqueue(tooManyRequests(retryAfter = "0"))
        server.enqueue(tooManyRequests(retryAfter = "0"))
        server.enqueue(ok())

        get(client()).use { response -> assertEquals(200, response.code) }

        assertEquals(listOf(10_000L, 2_000L, 4_000L), sleeps)
    }

    @Test
    fun `a 429 pauses the following requests too`() {
        server.enqueue(tooManyRequests())
        server.enqueue(ok())
        server.enqueue(ok())
        val client = client()

        get(client).close()
        get(client).close()

        // backoff after the 429, then the regular interval before the next request
        assertEquals(listOf(1_000L, 300L), sleeps)
    }

    @Test
    fun `gives up after maxRetries and returns the 429`() {
        repeat(3) { server.enqueue(tooManyRequests()) }

        get(client(maxRetries = 2)).use { response -> assertEquals(429, response.code) }

        assertEquals(3, server.requestCount)
        assertEquals(listOf(1_000L, 2_000L), sleeps)
    }
}
