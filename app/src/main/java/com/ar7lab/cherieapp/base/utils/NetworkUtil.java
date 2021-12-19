package com.ar7lab.cherieapp.base.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

/**
 * This class will detect network connectivity based on wifi or mobile data
 */
public class NetworkUtil {
    private static final NetworkUtil ourInstance = new NetworkUtil();

    public static NetworkUtil getInstance() {
        return ourInstance;
    }

    private NetworkUtil() {
    }

    /**
     * Detect network connectivity based on wifi and mobile data
     * @param context to get system service for connectivity status
     * @return true if the device can access to internet, otherwise return false
     */
    public Boolean isInternetAvailable(Context context) {
        boolean result = false;
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (connectivityManager.getActiveNetwork() == null) {
                return false;
            }
            Network networkCapabilities = connectivityManager.getActiveNetwork();
            if (connectivityManager.getNetworkCapabilities(networkCapabilities) == null) {
                return false;
            }
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(networkCapabilities);
            if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) result = true;
            if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) result = true;
            if (actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) result = true;
        } else {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI
                    || type == ConnectivityManager.TYPE_MOBILE
                    || type == ConnectivityManager.TYPE_ETHERNET) {
                result = true;
            }
        }
        return result;
    }
}
