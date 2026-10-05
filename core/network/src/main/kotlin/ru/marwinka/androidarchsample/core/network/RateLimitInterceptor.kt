package ru.marwinka.androidarchsample.core.network

import okhttp3.Interceptor
import okhttp3.Response

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val MILLIS_IN_SECOND = 1_000L
private const val NANOS_IN_MILLI = 1_000_000L

/**
 * Keeps all requests of one OkHttpClient under the server's rate limit.
 *
 * - Paces requests: attempts start at least [minIntervalMillis] apart, across threads. The API
 *   and image loading share the client, so they share the limit as the server counts it.
 * - On HTTP 429 pauses every request of the client, for `Retry-After` seconds when it is positive,
 *   otherwise for an exponential backoff, and retries up to [maxRetries] times.
 *
 * [clock] and [sleep] are injected so tests do not wait in real time.
 */
class RateLimitInterceptor(
    private val minIntervalMillis: Long = 300,
    private val maxRetries: Int = 5,
    private val backoffMillis: Long = 1_000,
    private val maxDelayMillis: Long = 30_000,
    private val clock: () -> Long = { System.nanoTime() / NANOS_IN_MILLI },
    private val sleep: (Long) -> Unit = Thread::sleep,
) : Interceptor {
    private val lock = Any()
    private var nextSlotAt = Long.MIN_VALUE

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        while (true) {
            awaitSlot()
            val response = chain.proceed(chain.request())
            if (response.code != HTTP_TOO_MANY_REQUESTS || attempt == maxRetries) return response
            attempt++
            val delay = retryDelay(response, attempt)
            response.close()
            pauseAll(delay)
        }
    }

    /** Reserves the next free slot, then waits for it outside the lock. */
    private fun awaitSlot() {
        val wait =
            synchronized(lock) {
                val now = clock()
                val slot = maxOf(now, nextSlotAt)
                nextSlotAt = slot + minIntervalMillis
                slot - now
            }
        if (wait > 0) sleep(wait)
    }

    private fun pauseAll(delayMillis: Long) {
        synchronized(lock) { nextSlotAt = maxOf(nextSlotAt, clock() + delayMillis) }
    }

    // Cloudflare answers `Retry-After: 0` while the block is still active, so 0 means "unknown".
    private fun retryDelay(
        response: Response,
        attempt: Int,
    ): Long {
        val retryAfter =
            response
                .header("Retry-After")
                ?.toLongOrNull()
                ?.takeIf { it > 0 }
                ?.times(MILLIS_IN_SECOND)
        val backoff = backoffMillis shl (attempt - 1)
        return (retryAfter ?: backoff).coerceAtMost(maxDelayMillis)
    }
}
