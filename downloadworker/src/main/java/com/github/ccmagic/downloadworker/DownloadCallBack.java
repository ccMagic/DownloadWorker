package com.github.ccmagic.downloadworker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;
import io.reactivex.ObservableEmitter;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author kxmc
 * 2019/3/8 10:06
 * <p>
 * 下载回调
 */
public class DownloadCallBack implements Callback {

    private static final String TAG = "DownloadCallBack";

    /**
     * 下载参数
     */
    private DownloadBuilder downloadBuilder;
    /**
     * 下载信息传递
     */
    private RxEmitterEntity rxEmitterEntity = new RxEmitterEntity();
    private ObservableEmitter<RxEmitterEntity> emitter;

    private boolean isGetLength;

    DownloadCallBack(boolean isGetLength, DownloadBuilder builder, ObservableEmitter<RxEmitterEntity> emitter) {
        this.downloadBuilder = builder;
        this.emitter = emitter;
        this.isGetLength = isGetLength;
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        rxEmitterEntity.setCode(RxEmitterEntity.FAIL);
        rxEmitterEntity.setThrowable(e);
        emitter.onNext(rxEmitterEntity);
        emitter.onError(e);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) {
        if (!response.isSuccessful()) {
            String message = null;
            try {
                message = response.body().string();
            } catch (Exception e) {
                rxEmitterEntity.setCode(RxEmitterEntity.FAIL);
                rxEmitterEntity.setMessage("下载失败，请重试");
                rxEmitterEntity.setThrowable(e);
                emitter.onNext(rxEmitterEntity);
                emitter.onError(e);
            }
            Exception e = new Exception("请求失败：\n" + message);
            rxEmitterEntity.setCode(RxEmitterEntity.FAIL);
            rxEmitterEntity.setMessage("下载失败，请重试");
            rxEmitterEntity.setThrowable(e);
            emitter.onNext(rxEmitterEntity);
            emitter.onError(e);
            return;
        }
        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            Exception e = new Exception("responseBody is null");
            rxEmitterEntity.setCode(RxEmitterEntity.FAIL);
            rxEmitterEntity.setMessage("下载失败，请重试");
            rxEmitterEntity.setThrowable(e);
            emitter.onNext(rxEmitterEntity);
            emitter.onError(e);
            return;
        }
        if (isGetLength) {
            long totalLong = responseBody.contentLength();

            //把长度保存到本地，断点续传的时候判断是否需要重新下载，还是直接使用本地下载好的
            SharedPreferences sharedPreferences = downloadBuilder.getBuildInfo()
                    .getApplication()
                    .getSharedPreferences(RxDownloadManager.DOWNLOAD_SP_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putLong(downloadBuilder.getBuildInfo().getUri(), totalLong);
            editor.apply();
            //
            rxEmitterEntity.setCode(RxEmitterEntity.TOTAL_LENGTH);
            rxEmitterEntity.setTotalSize(totalLong);
            emitter.onNext(rxEmitterEntity);
            //重新设置已经下载的内容
            downloadBuilder.getBuildInfo().getLocalInfo().setDownloadedLength(downloadBuilder.getBuildInfo().getLocalInfo().getDownloadedLength());
            rxEmitterEntity.setProgress(downloadBuilder.getBuildInfo().getLocalInfo().getDownloadedLength());
            rxEmitterEntity.setCode(RxEmitterEntity.LOADING);
            emitter.onNext(rxEmitterEntity);
            //如果是获取下载总量
            return;
        }

        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        RandomAccessFile randomAccessFile = null;
        try {

            inputStream = responseBody.byteStream();
            bufferedInputStream = new BufferedInputStream(inputStream);

            //断点续传关键代码
            randomAccessFile = new RandomAccessFile(downloadBuilder.getBuildInfo().getLocalInfo().getLocalFile(), "rwd");
            randomAccessFile.seek(downloadBuilder.getBuildInfo().getLocalInfo().getDownloadedLength());

            int len = -1;
            byte[] buff = new byte[1024];
            while ((len = bufferedInputStream.read(buff)) != -1) {
                //
                randomAccessFile.write(buff, 0, len);
                //重新设置已经下载的内容
                downloadBuilder.getBuildInfo().getLocalInfo().setDownloadedLength((downloadBuilder.getBuildInfo().getLocalInfo().getDownloadedLength() + len));
                rxEmitterEntity.setProgress(downloadBuilder.getBuildInfo().getLocalInfo().getDownloadedLength());
                rxEmitterEntity.setCode(RxEmitterEntity.LOADING);
                emitter.onNext(rxEmitterEntity);
            }

            rxEmitterEntity.setCode(RxEmitterEntity.COMPLETE);
            rxEmitterEntity.setPath(downloadBuilder.getBuildInfo().getLocalInfo().getLocalFile().getAbsolutePath());
            rxEmitterEntity.setMessage("下载成功");
            emitter.onNext(rxEmitterEntity);
            emitter.onComplete();
        } catch (IOException e) {
            //
            rxEmitterEntity.setCode(RxEmitterEntity.LOAD_FAIL);
            rxEmitterEntity.setMessage("下载出错，请重试");
            rxEmitterEntity.setThrowable(e);
            emitter.onNext(rxEmitterEntity);
            emitter.onError(e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "onResponse: ", e);
            }

            try {
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "onResponse: ", e);
            }

            try {
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "onResponse: ", e);
            }
        }
    }
}
