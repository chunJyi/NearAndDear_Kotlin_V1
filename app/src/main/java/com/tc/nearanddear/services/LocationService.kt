package com.tc.nearanddear.services

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
        var locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10.0f); // 10 meters

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                for (location in result.locations) {
                    val lat = location.latitude
                    val lng = location.longitude
                    Log.d("LocationService", "Lat: $lat, Lng: $lng")
                    UserSession.loginUser?.location_model = LocationModel(lat, lng)
                    CoroutineScope(Dispatchers.IO).launch {
                        pushLocationToSupabase(lat, lng)
                    }
                    updateNotification(lat, lng)
                }
            }
        }
    }

    suspend fun pushLocationToSupabase(lat: Double, lng: Double) {
        val userId = UserSession.loginUser?.userID ?: return // Make sure we have a valid userId

        val updateData = mapOf(
            "location_model" to mapOf(
                "latitude" to lat,
                "longitude" to lng
            )
        )
        try {

            val response = client
                .from("loginUser")
                .update(updateData) {
                    filter {
                        eq("userID", userId)
                    }
                }
            println(response.data)
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
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 50000L)
            .setMinUpdateDistanceMeters(10f) // Only update if moved 10 meters
            .build()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Tracking Active")
            .setContentText(contentText)
            .setSmallIcon(com.google.android.gms.base.R.drawable.googleg_disabled_color_18) // Replace with your own icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(lat: Double, lng: Double) {
        val updatedNotification = createNotification("Lat: $lat, Lng: $lng")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, updatedNotification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "Location Service",
            NotificationManager.IMPORTANCE_LOW
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
