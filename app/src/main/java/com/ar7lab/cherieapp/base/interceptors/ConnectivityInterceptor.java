package com.ar7lab.cherieapp.base.interceptors;

import android.content.Context;

import androidx.annotation.NonNull;

import com.ar7lab.cherieapp.base.utils.NetworkUtil;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import com.ar7lab.cherieapp.base.exception.NoInternetException;

/**
 * Connectivity Interceptor for okhttp client to return a network status message
 */
public class ConnectivityInterceptor implements Interceptor {

    private Context mContext;

    public ConnectivityInterceptor(Context context) {
        mContext = context;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        if (!NetworkUtil.getInstance().isInternetAvailable(mContext)) {
            throw new NoInternetException("No internet connection. Please check your connection and try again.");
        }

        Request.Builder builder = chain.request().newBuilder();
        return chain.proceed(builder.build());
    }
}
