package com.github.ccmagic.downloadworker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * @author kxmc
 * 2019/3/8 9:48
 * <p>
 */
public class DownloadBuilder {

    private static final String TAG = "DownloadBuilder";
    /**
     * 保存下载构造器设置的信息
     */
    private DownloadBuilderInfo mBuilderInfo;

    public DownloadBuilderInfo getBuildInfo() {
        return mBuilderInfo;
    }

    static DownloadBuilder builder() {
        LocalInfo localInfo = new LocalInfo();
        DownloadBuilderInfo builderInfo = new DownloadBuilderInfo();
        builderInfo.setLocalInfo(localInfo);
        return new DownloadBuilder(builderInfo);
    }

    private DownloadBuilder(DownloadBuilderInfo builderInfo) {
        this.mBuilderInfo = builderInfo;
    }


    /**
     * 超时时间
     * 单位：秒
     */
    public DownloadBuilder timeout(long connectTimeout, long readTimeout, long writeTimeout) {
        mBuilderInfo.setConnectTimeout(connectTimeout);
        mBuilderInfo.setReadTimeout(readTimeout);
        mBuilderInfo.setWriteTimeout(writeTimeout);
        return this;
    }

    /**
     * 文件保存路径
     */
    public DownloadBuilder savePath(String path) {
        mBuilderInfo.getLocalInfo().setSavePath(path);
        return this;
    }

    /**
     * 文件名称
     */
    public DownloadBuilder fileName(String fileName) {
        mBuilderInfo.getLocalInfo().setFileName(fileName);
        return this;
    }

    public DownloadBuilder setApplication(Application application) {
        mBuilderInfo.setApplication(application);
        return this;
    }

    /**
     * 设置下载地址
     */
    public DownloadBuilder downloadUri(String uri) {
        mBuilderInfo.setUri(uri);
        return this;
    }

    /**
     * 设计下载监听
     */
    public DownloadBuilder setDownloadListener(DownloadListener downloadListener) {
        mBuilderInfo.setDownloadListener(downloadListener);
        return this;
    }


    /**
     *
     * */
    public RxDownloadManager build() {

        if (mBuilderInfo.getApplication() == null) {
            throw new IllegalArgumentException("please setApplication");
        }
        if (TextUtils.isEmpty(mBuilderInfo.getUri())) {
            throw new IllegalArgumentException("download uri can not be null!!!");
        }

        //如果没有设置文件名称，默认使用下载地址最后的文件名称
        if (TextUtils.isEmpty(mBuilderInfo.getLocalInfo().getFileName())) {
            String fileName = mBuilderInfo.getUri().substring(mBuilderInfo.getUri().lastIndexOf("/") + 1);
            if (TextUtils.isEmpty(fileName)) {
//                throw new IllegalArgumentException("Are you sure you uri is right? -->" + mBuilderInfo.getUri());
                fileName = "错误文件";
            }
            mBuilderInfo.getLocalInfo().setFileName(fileName);
        }
        SharedPreferences sp = mBuilderInfo.getApplication().getSharedPreferences(RxDownloadManager.DOWNLOAD_SP_NAME, Context.MODE_PRIVATE);
        //如果取不到总大小，保证能正常下载，否则没进行过下载的情况下，默认相等，无法进行下载
        long totalLength = sp.getLong(mBuilderInfo.getUri(), 0);
        long downloadedLength = mBuilderInfo.getLocalInfo().getDownloadedLength();
        if (mBuilderInfo.getDownloadListener() != null) {
            mBuilderInfo.getDownloadListener().totalLength(totalLength);
            mBuilderInfo.getDownloadListener().loading(downloadedLength);
        }
        //
        return new RxDownloadManager(this);
    }
}
