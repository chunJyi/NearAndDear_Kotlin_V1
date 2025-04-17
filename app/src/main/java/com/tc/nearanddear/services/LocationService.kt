package com.tc.nearanddear.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.TrafficStats
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.tc.nearanddear.data.SupabaseClientProvider.client
import com.tc.nearanddear.model.LocationModel
import com.tc.nearanddear.session.UserSession
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okio.utf8Size
import java.time.Instant
import java.util.Locale.filter

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val channelId = "location_channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    val lat = location.latitude.toString()
                    val lng = location.longitude.toString()
                    Log.d("LocationService", "Lat: $lat, Lng: $lng")
                    UserSession.loginUser = UserSession.loginUser?.copy(
                        location_model = LocationModel(lat, lng)
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        checkBandwidth {
                            pushLocationToSupabase(lat, lng)
                        }
                    }
                    updateNotification(lat, lng)
                }
            }
        }
    }

    // Bandwidth checker: accepts a suspend lambda
    suspend fun checkBandwidth(block: suspend () -> Unit) {
        val txBefore = TrafficStats.getUidTxBytes(android.os.Process.myUid())
        val rxBefore = TrafficStats.getUidRxBytes(android.os.Process.myUid())

        block() // perform the actual network call

        val txAfter = TrafficStats.getUidTxBytes(android.os.Process.myUid())
        val rxAfter = TrafficStats.getUidRxBytes(android.os.Process.myUid())

        val sent = txAfter - txBefore
        val received = rxAfter - rxBefore

        Log.d("Bandwidth", "Sent: $sent bytes, Received: $received bytes")
    }

    suspend fun pushLocationToSupabase(lat: String, lng: String) {
        val userId = UserSession.loginUser?.userID ?: return // Make sure we have a valid userId


        val locationJson = mapOf(
            "latitude" to lat,
            "longitude" to lng,
            "updatedAt" to Instant.now().toString()
        )

        val updateData = mapOf(
            "location_model" to locationJson
        )

        try {
            val response = client.from("loginUser").update(updateData) {
                filter {
                    eq("userID", userId)
                }
            }
            println(response.data.utf8Size())
        } catch (e: Exception) {
            println("Error updating location: ${e.message}")
        }

    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(notificationId, createNotification("Getting location..."))
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000L)
//            .setMinUpdateDistanceMeters(10f) // Only update if moved 10 meters
            .build()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            )
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking Active").setContentText(contentText)
            .setSmallIcon(com.google.android.gms.base.R.drawable.googleg_disabled_color_18) // Replace with your own icon
            .setPriority(NotificationCompat.PRIORITY_LOW).build()
    }

    private fun updateNotification(lat: String, lng: String) {
        val updatedNotification = createNotification("Lat: $lat, Lng: $lng")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, updatedNotification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId, "Location Service", NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
