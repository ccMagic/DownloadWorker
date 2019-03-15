package com.github.ccmagic.downloadworker;

import android.app.Application;

/**
 * @author kxmc
 * 2019/3/8 14:15
 */
public class DownloadBuilderInfo {

    /**
     * 超时时间默认15秒
     */
    private long connectTimeout = 15;
    private long readTimeout = 15;
    private long writeTimeout = 15;
    private Application application;

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    private String uri;
    private DownloadListener downloadListener;
    private LocalInfo localInfo;


    public long getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public long getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(long readTimeout) {
        this.readTimeout = readTimeout;
    }

    public long getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(long writeTimeout) {
        this.writeTimeout = writeTimeout;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public DownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void setDownloadListener(DownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }

    public LocalInfo getLocalInfo() {
        return localInfo;
    }

    public void setLocalInfo(LocalInfo localInfo) {
        this.localInfo = localInfo;
    }
}
