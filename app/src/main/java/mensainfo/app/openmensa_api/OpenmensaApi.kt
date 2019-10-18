package mensainfo.app.openmensa_api

import android.content.Context
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonRequest
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import java.nio.charset.Charset
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object OpenmensaApi {
    private lateinit var queue: RequestQueue
    private lateinit var scheduler: ScheduledExecutorService

    fun init(ctx: Context) {
        queue = Volley.newRequestQueue(ctx)
        scheduler = Executors.newSingleThreadScheduledExecutor()
    }

    object Requests {
        fun getCanteens(consumer: (Canteens) -> Unit) {
            getPaginatedArray(
                    { consumer(Canteens(it)) },
                    { JSONArray(it).objMap(::Canteen) },
                    "canteens"
            )
        }

        fun getDays(id: Int, consumer: (List<Day>) -> Unit) {
            get(
                    consumer,
                    { JSONArray(it).objMap(::Day) },
                    "canteens/$id/days"
            )
        }

        fun getMeals(id: Int, day: String, consumer: (List<Meal>) -> Unit) {
            get(
                    consumer,
                    { JSONArray(it).objMap(::Meal) },
                    "canteens/$id/days/$day/meals"
            )
        }

        fun synchronised(timeoutMs: Long, f: (Requests).() -> Unit) {
            this.f()
            scheduler.awaitTermination(timeoutMs, TimeUnit.MILLISECONDS)
        }
    }

    private fun <T> getPaginatedArray(consumer: (List<T>) -> Unit, map: (String) -> Iterable<T>, url: String) {
        val complete = "$API_ENDPOINT$url?limit=100&page="
        val all = mutableListOf<T>()
        scheduler.schedule({
            var i = 1
            do {
                val result = get(complete + i, map)
                i++
            } while (result != null && all.addAll(result))

            consumer(all)
        }, 0, TimeUnit.SECONDS)
    }

    private fun <T> get(consumer: (T) -> Unit, map: (String) -> T, url: String) {
        val complete = "$API_ENDPOINT$url"
        val cache = queue.cache[complete]
        if (cache != null && !cache.isExpired) {
            val response = parseNetworkResponse(
                    NetworkResponse(
                            304,
                            cache.data,
                            true,
                            0,
                            cache.allResponseHeaders), map)
            consumer(response.result)
        } else {
            queue.cache.remove(complete)

            scheduler.schedule({
                val result = get(complete, map)
                if (result != null) {
                    consumer(result)
                }
            }, 0, TimeUnit.SECONDS)
        }
    }

    private fun <T> get(url: String, map: (String) -> T): T? {
        val future = RequestFuture.newFuture<T>()
        val request = APIRequest<T>(url, map, future)
        queue.add(request)

        return try {
            future.get()
        } catch (e: InterruptedException) {
            null
        } catch (e: ExecutionException) {
            null
        }
    }

    private fun parseSetCache(response: NetworkResponse): Cache.Entry {
        val date = when (val headerDate = response.headers["Date"]) {
            null -> 0
            else -> HttpHeaderParser.parseDateAsEpoch(headerDate)
        }

        val expires = System.currentTimeMillis() + 600000
        return Cache.Entry().apply {
            data = response.data
            softTtl = expires
            ttl = expires
            serverDate = date
            responseHeaders = response.headers
        }
    }

    private fun <T> parseNetworkResponse(response: NetworkResponse, map: (String) -> T): Response<T> {
        val result = String(response.data, Charset.forName(HttpHeaderParser.parseCharset(response.headers)))
        val resp = map(result)

        return Response.success(resp, parseSetCache(response))
    }

    private class APIRequest<T>(private val requestUrl: String, private val map: (String) -> T, future: RequestFuture<T>)
        : JsonRequest<T>(Method.GET, requestUrl, "", future, future) {

        init {
            retryPolicy = DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS, Int.MAX_VALUE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        }

        override fun parseNetworkResponse(response: NetworkResponse?) = parseNetworkResponse(response!!, map)

        override fun getCacheKey(): String = requestUrl
    }
}