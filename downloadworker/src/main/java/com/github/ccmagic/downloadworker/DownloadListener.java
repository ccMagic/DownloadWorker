package com.github.ccmagic.downloadworker;

import androidx.annotation.Nullable;

/**
 * @author kxmc
 * 2019/3/8 9:53
 */
public interface DownloadListener {

    /**
     * 开始下载
     */
    void start();


    /**
     * 下载内容的总大小
     */
    void totalLength(long total);

    /**
     * 正在下载
     *
     * @param progress 下载进度
     */
    void loading(long progress);

    /**
     * 暂停下载
     */
    void pause();

    /**
     * 下载取消
     */
    void cancel();

    /**
     * 下载完成
     */
    void complete(String path);

    /**
     * 请求失败
     */
    void fail(@Nullable String message, @Nullable Throwable throwable);

    /**
     * 下载过程中失败
     */
    void loadFail(String message, @Nullable Throwable throwable);
}
