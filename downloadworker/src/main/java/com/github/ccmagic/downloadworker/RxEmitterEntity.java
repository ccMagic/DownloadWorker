package com.github.ccmagic.downloadworker;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

/**
 * @author kxmc
 * 2019/3/8 17:49
 * <p>
 * 下载信息传递类，用于RxJava发射数据
 */
class RxEmitterEntity {

    static final int START = 1;//开始
    static final int TOTAL_LENGTH = 2;//总大小
    static final int LOADING = 3;//正在下载
    static final int PAUSE = 4;//暂停
    static final int CANCEL = 5;//取消
    static final int COMPLETE = 6;//完成
    static final int FAIL = 7;//失败
    static final int LOAD_FAIL = 8;//下载中断

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({START, TOTAL_LENGTH, LOADING, PAUSE, CANCEL, COMPLETE, FAIL, LOAD_FAIL})
    @interface EmitterCodeType {
    }

    private int code;
    private String message;
    private Throwable throwable;
    private long totalSize;
    private long progress;
    //本地保存路径
    private String path;

    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    int getCode() {
        return code;
    }

    void setCode(@EmitterCodeType int code) {
        this.code = code;
    }

    String getMessage() {
        return message;
    }

    void setMessage(String message) {
        this.message = message;
    }

    Throwable getThrowable() {
        return throwable;
    }

    void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    long getTotalSize() {
        return totalSize;
    }

    void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    long getProgress() {
        return progress;
    }

    void setProgress(long progress) {
        this.progress = progress;
    }
}
