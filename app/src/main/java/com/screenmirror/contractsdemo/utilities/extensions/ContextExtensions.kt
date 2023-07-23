package com.screenmirror.contractsdemo.utilities.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.screenmirror.contractsdemo.MainHolderActivity

object ContextExtensions {

    private const val NETWORK_TYPE_WIFI = "WiFi"
    private const val NETWORK_TYPE_MOBILE = "Mobile EDGE>"
    private const val NETWORK_TYPE_OTHERS = "Others>"

    private const val WIFI_DISABLED = "<disabled>"
    private const val WIFI_NO_CONNECT = "<not connect>"
    private const val WIFI_NO_PERMISSION = "<permission deny>"

    private const val UNKNOWN = "<unknown>"

    fun Context.getCurrentNetworkDetail(): String {
        val connManager =
            this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
        if (networkInfo?.isConnected == true) {
            val wifiManager =
                this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val connectionInfo = wifiManager.connectionInfo
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.ssid)) {
                Log.e("ssid", connectionInfo.ssid)

                return connectionInfo.ssid
            }
        } else {
            Log.e("ssid", "No Connection")

            return "Not Connected"
        }
        return "Not Connected"

    }

    fun Context.getWiFiInfoSSID(): String {
        val wifiManager: WifiManager = getSystemService<WifiManager>(this, Context.WIFI_SERVICE)
        if (!wifiManager.isWifiEnabled) return WIFI_DISABLED
        val wifiInfo = wifiManager.connectionInfo ?: return WIFI_NO_CONNECT
        return if (wifiInfo.ssid == WifiManager.UNKNOWN_SSID) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED
                ) {
                    return WIFI_NO_PERMISSION
                }
                if (wifiManager.configuredNetworks != null) {
                    for (config in wifiManager.configuredNetworks!!) {
                        if (config.networkId == wifiInfo.networkId) {
                            return config.SSID.replace("\"".toRegex(), "")
                        }
                    }
                }
            } else {
                return WIFI_NO_CONNECT
            }
            UNKNOWN
        } else {
            wifiInfo.ssid.replace("\"".toRegex(), "")
        }
    }

    private fun <T : Any?> getSystemService(context: Context, name: String): T {
        return context.applicationContext.getSystemService(name) as T
    }

    val Fragment.mainHolderActivity: MainHolderActivity?
        get() {
            return activity as MainHolderActivity?
        }
}