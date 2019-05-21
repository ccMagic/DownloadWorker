package com.github.ccmagic.downloadworker;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * @author kxmc
 * 2019/3/8 17:36
 */
public class RxDownloadSubscribe implements ObservableOnSubscribe<RxEmitterEntity> {

    private static final String TAG = "RxDownloadSubscribe";
    private DownloadBuilder downloadBuilder;
    private boolean isGetLength;

    public RxDownloadSubscribe(boolean isGetLength, DownloadBuilder downloadBuilder) {
        this.isGetLength = isGetLength;
        this.downloadBuilder = downloadBuilder;
    }

    @Override
    public void subscribe(ObservableEmitter<RxEmitterEntity> emitter) throws Exception {
        //
        long downloadedLength = downloadBuilder.getBuildInfo().getLocalInfo().getDownloadedLength();
        //显示已下载的进度
        RxEmitterEntity rxEmitterEntity = new RxEmitterEntity();

        //
        SharedPreferences sp = downloadBuilder.getBuildInfo().getApplication().getSharedPreferences(RxDownloadManager.DOWNLOAD_SP_NAME, Context.MODE_PRIVATE);
        //如果取不到总大小，保证能正常下载，否则没进行过下载的情况下，默认相等，无法进行下载
        long totalLength = sp.getLong(downloadBuilder.getBuildInfo().getUri(), (downloadedLength + 1));
        if (!isGetLength) {
            if (downloadedLength - totalLength >= -2) {//-2给点幅度空间，防止一点误差导致逻辑出错
                rxEmitterEntity.setCode(RxEmitterEntity.COMPLETE);
                rxEmitterEntity.setPath(downloadBuilder.getBuildInfo().getLocalInfo().getPath());
                rxEmitterEntity.setMessage("下载成功2");
                emitter.onNext(rxEmitterEntity);
                emitter.onComplete();
                return;
            }
        }
        //
        OkHttpClient okHttpClient = OkHttpManager.builder()
                .setConnectTimeout(downloadBuilder.getBuildInfo().getConnectTimeout())
                .setReadTimeout(downloadBuilder.getBuildInfo().getReadTimeout())
                .setWriteTimeout(downloadBuilder.getBuildInfo().getWriteTimeout())
                .build();

        Request request;
        if (isGetLength) {
            request = new Request.Builder()
                    .url(downloadBuilder.getBuildInfo().getUri())
                    .build();
        } else {
            request = new Request.Builder()
                    .url(downloadBuilder.getBuildInfo().getUri())
                    .header("RANGE", "bytes=" + downloadedLength + "-")//断点续传
                    .build();
        }

        Call call = okHttpClient.newCall(request);

        //开始下载
        if (!isGetLength) {
            rxEmitterEntity.setCode(RxEmitterEntity.START);
            emitter.onNext(rxEmitterEntity);
        }
        call.enqueue(new DownloadCallBack(isGetLength, downloadBuilder, emitter));
    }
}
