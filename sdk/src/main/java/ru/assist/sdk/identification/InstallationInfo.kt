package ru.assist.sdk.identification

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import ru.assist.sdk.R

object InstallationInfo {
    private const val INSTALLATION = ".mobilepayinstallation"
    private const val APP_REG_ID = "ApplicationRegId"
    private const val ID_MAX_LENGTH = 50

    private lateinit var settings: SharedPreferences
    private var appName: String? = null
    private var versionName: String? = null

    fun generateDeviceId(): String {
        val androidSdk = Build.VERSION.SDK_INT
        val model = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        var uniqueId = String.format(
            java.util.Locale.US,
            "%1\$d,%2\$s,%3$.15s,%4\$s",
            androidSdk,
            manufacturer,
            model,
            java.util.UUID.randomUUID().toString()
        )
            .replace("(", "")
            .replace(")", "")
            .replace("<", "")
            .replace(">", "")
            .replace("=", "")
            .replace("'", "")
            .replace(";", "")
            .replace("#", "")
            .replace("/", "")
        if (uniqueId.length >= ID_MAX_LENGTH) {
            uniqueId = uniqueId.substring(0, ID_MAX_LENGTH - 1)
        }
        return uniqueId
    }

    fun init(context: Context): InstallationInfo {
        getNames(context)
        val name = context.applicationInfo.packageName + INSTALLATION
        settings = context.getSharedPreferences(name, Context.MODE_PRIVATE)
        return this
    }

    fun getAppRegId(): String? {
        val deviceId = settings.getString(APP_REG_ID, null)
        return if (deviceId != null) settings.getString(deviceId, null) else null
    }

    fun getDeviceUniqueId(): String? =
        settings.getString(APP_REG_ID, null)

    fun setAppRegID(deviceId: String, registrationID: String) {
        settings.edit()
            .putString(APP_REG_ID, deviceId)
            .putString(deviceId, registrationID)
            .commit()
    }

    fun clearAppRegID() {
        val deviceId = settings.getString(APP_REG_ID, null)
        if (deviceId != null) {
            settings.edit()
                .putString(APP_REG_ID, null)
                .putString(deviceId, null)
                .apply()
        }
    }

    fun appName(): String? {
        return appName
    }

    fun versionName(): String? {
        return versionName
    }

    private fun getNames(context: Context) {
        appName = context.resources.getString(R.string.app_name)
        versionName = ""
        val manager = context.packageManager
        try {
            val pacInfo = manager.getPackageInfo(context.packageName, 0)
            appName = pacInfo.packageName
            versionName = pacInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("InstInfo", "Error initializing appName and versionName", e)
        }
    }
}