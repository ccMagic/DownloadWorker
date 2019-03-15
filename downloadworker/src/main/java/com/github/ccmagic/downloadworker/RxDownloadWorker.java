package com.github.ccmagic.downloadworker;

import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author kxmc
 * 2019/3/8 11:52
 * <p>
 * 执行下载操作
 */
class RxDownloadWorker {
    private static final String TAG = "RxDownloadWorker";

    static Disposable start(boolean isGetLength,DownloadBuilder downloadBuilder) {
        DownloadListener downloadListener = downloadBuilder.getBuildInfo().getDownloadListener();
        return Observable.create(new RxDownloadSubscribe(isGetLength,downloadBuilder))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(rxEmitterEntity -> {
                    Log.i(TAG, "onNext: ");
                    if (rxEmitterEntity != null && downloadListener != null) {
                        switch (rxEmitterEntity.getCode()) {
                            case RxEmitterEntity.START://开始
                                downloadListener.start();
                                break;
                            case RxEmitterEntity.TOTAL_LENGTH://总大小
                                downloadListener.totalLength(rxEmitterEntity.getTotalSize());
                                break;
                            case RxEmitterEntity.LOADING://正在下载
                                downloadListener.loading(rxEmitterEntity.getProgress());
                                break;
                            case RxEmitterEntity.PAUSE://暂停
                                downloadListener.pause();
                                break;
                            case RxEmitterEntity.CANCEL://取消
                                downloadListener.cancel();
                                break;
                            case RxEmitterEntity.COMPLETE://完成
                                downloadListener.complete(rxEmitterEntity.getPath());
                                break;
                            case RxEmitterEntity.FAIL://失败
                                downloadListener.fail(rxEmitterEntity.getMessage(), rxEmitterEntity.getThrowable());
                                break;
                            case RxEmitterEntity.LOAD_FAIL://下载中断
                                downloadListener.loadFail(rxEmitterEntity.getMessage(), rxEmitterEntity.getThrowable());
                                break;
                            default:
                                break;
                        }
                    }
                }, throwable -> {
                    Log.e(TAG, "onError: ", throwable);
                    downloadListener.fail("下载失败，请重试", throwable);
                }, () -> Log.i(TAG, "onComplete: "));

    }
}
