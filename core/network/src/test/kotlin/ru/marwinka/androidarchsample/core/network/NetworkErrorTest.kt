package ru.marwinka.androidarchsample.core.network

import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import ru.marwinka.androidarchsample.core.common.AppError
import java.io.IOException

class NetworkErrorTest {
    private fun httpError(code: Int) = HttpException(Response.error<Unit>(code, "".toResponseBody(null)))

    @Test
    fun `IOException maps to Network`() {
        assertTrue(IOException("offline").toNetworkError() is AppError.Network)
    }

    @Test
    fun `HTTP 404 maps to NotFound`() {
        assertTrue(httpError(404).toNetworkError() is AppError.NotFound)
    }

    @Test
    fun `other HTTP errors and unexpected exceptions map to Unknown`() {
        assertTrue(httpError(500).toNetworkError() is AppError.Unknown)
        assertTrue(IllegalStateException("bug").toNetworkError() is AppError.Unknown)
    }
}
