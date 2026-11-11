package com.danielagapov.spawn.ui.create

import android.content.Context
import android.location.Location
import com.danielagapov.spawn.R
import com.danielagapov.spawn.core.model.LocationDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class Place(val name: String, val address: String, val latitude: Double, val longitude: Double) {
    fun toLocation() = LocationDTO(name = if (address.isNotBlank()) "$name, $address" else name, latitude = latitude, longitude = longitude)
}

/** Place search + reverse geocoding via the Mapbox Geocoding API (same token the map uses). */
object PlaceSearch {
    private val client = OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS).build()
    private val json = Json { ignoreUnknownKeys = true }

    private fun token(ctx: Context) = ctx.getString(R.string.mapbox_access_token).trim()

    /** Typed query -> places near the user (Mapbox Search Box "forward"). */
    suspend fun search(ctx: Context, query: String, near: Location?): List<Place> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val url = "https://api.mapbox.com/search/searchbox/v1/forward".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("limit", "8")
            .addQueryParameter("access_token", token(ctx))
            .apply { near?.let { addQueryParameter("proximity", "${it.longitude},${it.latitude}") } }
            .build()
        fetchSearchBox(url.toString())
    }

    /** Empty query -> nearby spots to suggest, like the Figma list (cafes, restaurants, parks). */
    suspend fun nearby(ctx: Context, near: Location?): List<Place> = withContext(Dispatchers.IO) {
        if (near == null) return@withContext emptyList()
        listOf("cafe", "restaurant", "park").flatMap { cat ->
            val url = "https://api.mapbox.com/search/searchbox/v1/category/$cat?limit=3&proximity=${near.longitude},${near.latitude}&access_token=${token(ctx)}"
            fetchSearchBox(url)
        }.sortedBy { p -> FloatArray(1).also { Location.distanceBetween(near.latitude, near.longitude, p.latitude, p.longitude, it) }[0] }
    }

    private fun fetchSearchBox(url: String): List<Place> = try {
        client.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) return emptyList()
            val features = json.parseToJsonElement(r.body!!.string()).jsonObject["features"]?.jsonArray ?: return emptyList()
            features.mapNotNull { f ->
                val o = f.jsonObject
                val coords = o["geometry"]?.jsonObject?.get("coordinates")?.jsonArray ?: return@mapNotNull null
                val props = o["properties"]?.jsonObject ?: return@mapNotNull null
                val name = props["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val address = (props["address"]?.jsonPrimitive?.content?.let { a -> listOfNotNull(a, props["place_formatted"]?.jsonPrimitive?.content).joinToString(", ") }
                    ?: props["place_formatted"]?.jsonPrimitive?.content ?: "")
                Place(name, address, coords[1].jsonPrimitive.doubleOrNull ?: return@mapNotNull null, coords[0].jsonPrimitive.doubleOrNull ?: return@mapNotNull null)
            }
        }
    } catch (_: Exception) { emptyList() }

    suspend fun reverse(ctx: Context, lat: Double, lng: Double): Place = withContext(Dispatchers.IO) {
        val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$lng,$lat.json?limit=1&types=poi,address&access_token=${token(ctx)}"
        fetch(url).firstOrNull()?.copy(latitude = lat, longitude = lng)
            ?: Place("Pinned location", String.format("%.5f, %.5f", lat, lng), lat, lng)
    }

    private fun fetch(url: String): List<Place> = try {
        client.newCall(Request.Builder().url(url).build()).execute().use { r ->
            if (!r.isSuccessful) return emptyList()
            val features = json.parseToJsonElement(r.body!!.string()).jsonObject["features"]?.jsonArray ?: return emptyList()
            features.mapNotNull { f ->
                val o = f.jsonObject
                val center = o["center"]?.jsonArray ?: return@mapNotNull null
                val lng = center[0].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
                val lat = center[1].jsonPrimitive.doubleOrNull ?: return@mapNotNull null
                val text = o["text"]?.jsonPrimitive?.content ?: return@mapNotNull null
                val full = o["place_name"]?.jsonPrimitive?.content ?: text
                val address = full.removePrefix(text).trimStart(',', ' ').split(",").take(3).joinToString(",").trim()
                Place(text, address, lat, lng)
            }
        }
    } catch (_: Exception) { emptyList() }
}
