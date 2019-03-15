package com.github.ccmagic.downloadworker;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * @author kxmc
 * 2019/3/8 10:30
 */
class LocalInfo {

    private static final String TAG = "LocalInfo";

    /**
     * 本地已下载的文件的内容长度，用于支持断点续传
     */
    private long downloadedLength;
    /**
     * 设置的文件存储路径
     */
    private String savePath;
    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 本地保存的文件
     */
    private File localFile;


    String getPath() {
        return savePath;
    }

    void setSavePath(String path) {
        this.savePath = path;
    }

    String getFileName() {
        return fileName;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }


    long getDownloadedLength() {
        downloadedLength = getLocalFile().length();
        return downloadedLength;
    }

    /**
     * 已经下载完成的文件大小
     */
    void setDownloadedLength(long downloadedLength) {
        this.downloadedLength = downloadedLength;
    }


    /**
     * 获取本地下载的文件，若果本地没有则重新创建
     */
    File getLocalFile() {
        if (localFile == null) {
            String path;
            //todo 如果本地存在这个文件，则直接使用；如果没有，则重新创建，创建的过程中记得检验文件。。。
            if (TextUtils.isEmpty(savePath)) {
                path = Environment.getExternalStorageDirectory().getPath() + File.separator + "download" + File.separator + fileName;
                localFile = new File(path);
            } else {
                path = savePath + File.separator + fileName;
                localFile = new File(path);

            }
            if (!localFile.exists()) {
                File dir = new File(localFile.getParent());
                if (dir.mkdirs()) {
                    try {
                        localFile.createNewFile();
                    } catch (IOException e) {
                        Log.e(TAG, "getLocalFile: ", e);
                    }
                }
            } else {
                File dir = new File(localFile.getParent());
                if (dir.exists()) {
                    try {
                       localFile.createNewFile();
                    } catch (IOException e) {
                        Log.e(TAG, "getLocalFile: ", e);
                    }
                } else {
                    if (dir.mkdirs()) {
                        try {
                            localFile.createNewFile();
                        } catch (IOException e) {
                            Log.e(TAG, "getLocalFile: ", e);
                        }
                    }
                }
            }
            this.savePath = path;
        }
        return localFile;
    }

}
