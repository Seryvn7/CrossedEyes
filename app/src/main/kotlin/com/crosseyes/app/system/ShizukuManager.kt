package com.crosseyes.app.system

import android.util.Log
import rikka.shizuku.Shizuku

/**
 * Manages Shizuku connection and provides system-level operations.
 *
 * Shizuku enables root-like capabilities without actual root access by
 * running a privileged service via adb or device owner.
 */
object ShizukuManager {
    private const val TAG = "ShizukuManager"

    /** Whether Shizuku permission is currently granted */
    fun isPermissionGranted(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Request Shizuku permission from user.
     * Must be called from Activity's main thread.
     */
    fun requestPermission(requestCode: Int = 1001) {
        try {
            if (isPermissionGranted()) {
                Log.i(TAG, "Shizuku permission already granted")
                return
            }
            Shizuku.requestPermission(requestCode)
            Log.i(TAG, "Requested Shizuku permission (code $requestCode)")
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting Shizuku permission", e)
        }
    }

    /** Get Shizuku version info */
    fun getShizukuVersion(): Int {
        return try {
            Shizuku.getVersion()
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Get device info via Shizuku.
     */
    fun getDeviceInfo(): Map<String, String> {
        val info = mutableMapOf<String, String>()
        info["shizuku_version"] = getShizukuVersion().toString()
        info["permission_granted"] = isPermissionGranted().toString()
        return info
    }
}
