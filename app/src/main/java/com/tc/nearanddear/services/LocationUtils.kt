package com.tc.nearanddear.services

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.tc.nearanddear.model.LocationModel
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object LocationUtils {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LocationModel? {
        val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

        return suspendCancellableCoroutine { cont ->
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
//                        val address = getAddressFromLatLng(context, location.latitude, location.longitude)
                        val model = LocationModel(
                            latitude = location.latitude.toString(),
                            longitude = location.longitude.toString()
                        )
                        cont.resume(model)
                    } else {
                        cont.resume(null)
                    }
                }
                .addOnFailureListener {
                    cont.resume(null)
                }
        }
    }

//    suspend fun getAddressFromLatLng(
//        context: Context,
//        latitude: Double,
//        longitude: Double?
//    ): String {
//        val geocoder = Geocoder(context)
//        val addressList = geocoder.getFromLocation(latitude, longitude, 1)
//        return if (addressList != null && addressList.isNotEmpty()) {
//            val address = addressList[0]
//            "${address.getAddressLine(0)}, ${address.locality}, ${address.adminArea}, ${address.countryName}"
//        } else {
//            "Address not found"
//        }
//    }
}
