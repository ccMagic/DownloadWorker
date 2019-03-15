package com.github.ccmagic.downloadworker;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * @author kxmc
 * 2019/3/8 10:04
 */
class OkHttpManager {

    private OkHttpClient okHttpClient;

    private long connectTimeout = 15;
    private long readTimeout = 15;
    private long writeTimeout = 15;

    static OkHttpManager builder() {
        return new OkHttpManager();
    }

    OkHttpManager setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    OkHttpManager setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    OkHttpManager setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
        return this;
    }

    OkHttpClient build() {
        if (okHttpClient == null) {
            okHttpClient = new OkHttpClient().newBuilder()
                    .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                    .readTimeout(readTimeout, TimeUnit.SECONDS)
                    .writeTimeout(writeTimeout, TimeUnit.SECONDS)
//                .addInterceptor(addInterceptorHead())
//                .addInterceptor(addInterceptorResponse())
                    .build();
        }
        return okHttpClient;
    }


}
