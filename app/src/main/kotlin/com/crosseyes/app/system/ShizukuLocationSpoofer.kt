package com.crosseyes.app.system

import android.content.Context
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import rikka.shizuku.Shizuku

/**
 * Sets mock location using Shizuku's privileged service.
 */
object ShizukuLocationSpoofer {
    private const val TAG = "ShizukuLocationSpoofer"

    fun enableMockProvider(context: Context): Boolean {
        if (!ShizukuManager.isPermissionGranted()) {
            Log.w(TAG, "Shizuku permission not granted")
            return false
        }
        return try {
            Settings.Secure.putInt(
                context.contentResolver,
                "mock_location", 1
            )
            Log.i(TAG, "Enabled mock provider via Shizuku")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling mock provider", e)
            false
        }
    }

    fun disableMockProvider(context: Context): Boolean {
        if (!ShizukuManager.isPermissionGranted()) {
            Log.w(TAG, "Shizuku permission not granted")
            return false
        }
        return try {
            Settings.Secure.putInt(
                context.contentResolver,
                "mock_location", 0
            )
            Log.i(TAG, "Disabled mock provider via Shizuku")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling mock provider", e)
            false
        }
    }

    fun setLocation(context: Context, latitude: Double, longitude: Double,
                    altitude: Double = 0.0, accuracy: Float = 10f): Boolean {
        if (!ShizukuManager.isPermissionGranted()) {
            Log.w(TAG, "Shizuku permission not granted")
            return false
        }
        return try {
            enableMockProvider(context)
            val location = android.location.Location("gps")
            location.latitude = latitude
            location.longitude = longitude
            location.altitude = altitude
            location.accuracy = accuracy
            location.time = System.currentTimeMillis()
            val broadcast = android.content.Intent(
                android.location.LocationManager.PROVIDERS_CHANGED_ACTION
            )
            context.sendBroadcast(broadcast)
            Log.i(TAG, "Mock location set via Shizuku: $latitude, $longitude")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error setting mock location", e)
            false
        }
    }

    fun isMockLocationEnabled(context: Context): Boolean {
        return try {
            Settings.Secure.getInt(
                context.contentResolver,
                "mock_location", 0
            ) == 1
        } catch (e: Exception) {
            false
        }
    }
}
