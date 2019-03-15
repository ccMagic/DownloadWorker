package com.github.ccmagic.downloadworker;


import io.reactivex.disposables.Disposable;

/**
 * @author kxmc
 * 2019/3/8 13:49
 * 下载管理器
 */
public class RxDownloadManager {

    private static final String TAG = "RxDownloadManager";

    public static final String SP_KEY_TOTAL_LENGTH = "totalLength";
    public static final String DOWNLOAD_SP_NAME = "ccmagic_download_workman";


    private DownloadBuilder downloadBuilder;

    RxDownloadManager(DownloadBuilder builder) {
        this.downloadBuilder = builder;
    }


    /**
     * 开始构建下载任务
     */
    public static DownloadBuilder builder() {
        return DownloadBuilder.builder();
    }

    /**
     * 开始执行下载任务
     */
    public Disposable start() {
        return RxDownloadWorker.start(false, downloadBuilder);
    }

    /**
     * 获取下载总量
     */
    public Disposable requestLength() {
        return RxDownloadWorker.start(true, downloadBuilder);
    }

    /**
     * 暂停下载
     */
    public void stop() {
    }

    /**
     * 取消下载
     */
    public void cancel() {
    }
}
