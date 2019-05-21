package com.github.ccmagic.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.github.ccmagic.downloadworker.DownloadListener;
import com.github.ccmagic.downloadworker.RxDownloadManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

import io.reactivex.disposables.CompositeDisposable;


/**
 * @author kxmc
 * 2019/3/8 15:02
 */
public class DownloadFragmentDialog extends DialogFragment {
    /**
     * 取消和确定按钮
     */
    public static final int BTN_TYPE_CANCEL_AND_ENSURE = 1;
    /**
     * 只有取消按钮
     */
    public static final int BTN_TYPE_ONLY_ENSURE = 2;
    private DecimalFormat df = new DecimalFormat("0.00");

    /**
     * @param uri      文件下载地址，必填
     * @param fileName 文件名称、非必填
     * @param savePath 文件本地保存路径、非必填
     */
    public static DownloadFragmentDialog newInstance(
                                               String uri,
                                               String fileName,
                                               String savePath) {
        DownloadFragmentDialog dialogFragment = new DownloadFragmentDialog();
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        bundle.putString("fileName", fileName);
        bundle.putString("savePath", savePath);
        dialogFragment.setArguments(bundle);
        return dialogFragment;
    }

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    /**
     * 下载内容展示
     */
    private LinearLayout downLoadLayout;
    private ProgressBar progressBar;
    //
    private TextView progressTv;
    private TextView sizeProgressTv;

    private double each;
    private double totalSize;

    /**
     * wifi提示内容展示
     */
    private LinearLayout wifiTipsLayout;
    private TextView wifiCancelTv;
    private TextView wifiEnsureTv;

    /**
     * 下载管理器
     */
    private RxDownloadManager rxDownloadManager;
    private DownloadDialogListener downloadDialogListener;


    /**
     * 下载内容的设置信息
     */
    private String uri;
    private String fileName;
    private String savePath;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof DownloadDialogListener) {
            downloadDialogListener = (DownloadDialogListener) context;
        } else {
            throw new IllegalArgumentException("DownloadFragment`s Activity must implements com.github.ccmagic.downloadworkerman.dialog.DownloadFragment.DownloadDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.cc_magic_download_CenterDialogStyle);
        setCancelable(false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            uri = bundle.getString("uri");
            fileName = bundle.getString("fileName");
            savePath = bundle.getString("savePath");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cc_magic_download_manager_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        downLoadLayout = view.findViewById(R.id.downLoadLayout);
        progressBar = view.findViewById(R.id.progressBar);
        progressTv = view.findViewById(R.id.progressTv);
        sizeProgressTv = view.findViewById(R.id.sizeProgressTv);

        //
        wifiTipsLayout = view.findViewById(R.id.wifiTipsLayout);
        wifiCancelTv = view.findViewById(R.id.wifiCancelTv);
        wifiEnsureTv = view.findViewById(R.id.wifiEnsureTv);

        progressBar.setMax(100);
        setListener();

        progressBar.post(this::permission);

    }

    private void setListener() {
        wifiCancelTv.setOnClickListener(v -> dismiss());
        wifiEnsureTv.setOnClickListener(v -> doDownload());
    }

    /**
     * 获取权限
     */
    private void permission() {
        if (getActivity() != null) {
            int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
                initRxDownloadManager();
                wifiDetector();
            } else {
                //没有权限，向用户请求权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 15);
            }
        }
    }

    /**
     * wifi检测
     * */
    private void wifiDetector(){
        if (getActivity() != null) {
            //步骤1：通过Context.getSystemService(Context.CONNECTIVITY_SERVICE)获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            //步骤2：获取ConnectivityManager对象对应的NetworkInfo对象
            //NetworkInfo对象包含网络连接的所有信息
            //步骤3：根据需要取出网络连接信息
            //获取WIFI连接的信息
            NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            boolean isWifiConn = networkInfo.isConnected();
            if (!isWifiConn) {
                //非wifi连接，提示用户
                wifiTipsLayout.setVisibility(View.VISIBLE);
                downLoadLayout.setVisibility(View.GONE);
            } else {
                doDownload();
            }
        }
    }
    /**
     * 下载处理
     */
    private void doDownload() {
        if (getActivity() != null) {
            //wifi连接，直接下载
            wifiTipsLayout.setVisibility(View.GONE);
            downLoadLayout.setVisibility(View.VISIBLE);
            int hasWriteStoragePermission = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWriteStoragePermission == PackageManager.PERMISSION_GRANTED) {
                //拥有权限，执行操作
                if (compositeDisposable != null) {
                    if (rxDownloadManager == null) {
                        initRxDownloadManager();
                    }
                    compositeDisposable.add(rxDownloadManager.start());
                }
            } else {
                //没有权限，向用户请求权限
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 25);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 25) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意，执行操作
                wifiTipsLayout.setVisibility(View.GONE);
                downLoadLayout.setVisibility(View.VISIBLE);
                if (compositeDisposable != null) {
                    if (rxDownloadManager == null) {
                        initRxDownloadManager();
                    }
                    compositeDisposable.add(rxDownloadManager.start());
                }
            } else {
                //用户不同意，向用户展示该权限作用
                ToastTool.show("请在设置中打开“读写手机存储”的权限");
            }
        } else if (requestCode == 15) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initRxDownloadManager();
                wifiDetector();
            } else {
                //用户不同意，向用户展示该权限作用
                ToastTool.show("请在设置中打开“读写手机存储”的权限");
            }
        }
    }

    /**
     * 初始化设置
     */
    private void initRxDownloadManager() {

        if (getActivity() != null) {
            rxDownloadManager = RxDownloadManager.builder()
                    .setApplication(getActivity().getApplication())
                    .downloadUri(uri)
                    .timeout(15, 15, 15)
                    .fileName(fileName)
                    .savePath(savePath)
                    .setDownloadListener(new MyDownloadListener(this))
                    .build();
            rxDownloadManager.requestLength();
        }
    }

    private void start() {
    }


    private void totalLength(long total) {
        totalSize = total / 1024.0 / 1024.0;
        each = (total / 100.0);
    }


    private void loading(long progress) {
        if (each != 0) {
            int percentage = (int) (progress / each);
            progressBar.setProgress(percentage);
            progressTv.setText((percentage + "%"));
            sizeProgressTv.setText((df.format(progress / 1024.0 / 1024.0) + "/" + df.format(totalSize) + "M"));
//            if (percentage >= 100) {//下载完成
//                // ensureTv.setText("完成");
//            }
        }

    }


    private void pause() {

    }


    private void cancel() {

    }

    private void complete(String path) {
        if (downloadDialogListener != null) {
            downloadDialogListener.downloadComplete(path);
        }
        dismissAllowingStateLoss();
    }


    private void fail(@Nullable String message, @Nullable Throwable throwable) {
        ToastTool.show(message);
    }


    private void loadFail(String message, @Nullable Throwable throwable) {
        ToastTool.show(message);
    }

    @Override
    public void onDestroy() {
        if (compositeDisposable != null) {
            compositeDisposable.clear();
        }
        super.onDestroy();
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BTN_TYPE_CANCEL_AND_ENSURE, BTN_TYPE_ONLY_ENSURE})
    public @interface BtnType {
    }

    /**
     * 下载监听器
     */
    private static class MyDownloadListener implements DownloadListener {

        private WeakReference<DownloadFragmentDialog> fragmentWeakReference;

        private MyDownloadListener(DownloadFragmentDialog fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void start() {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().start();
            }
        }

        @Override
        public void totalLength(long total) {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().totalLength(total);
            }
        }

        @Override
        public void loading(long progress) {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().loading(progress);
            }
        }

        @Override
        public void pause() {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().pause();
            }
        }

        @Override
        public void cancel() {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().cancel();
            }
        }

        @Override
        public void complete(String path) {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().complete(path);
            }
        }

        @Override
        public void fail(@Nullable String message, @Nullable Throwable throwable) {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().fail(message, throwable);
            }
        }

        @Override
        public void loadFail(String message, @Nullable Throwable throwable) {
            if (fragmentWeakReference != null && fragmentWeakReference.get() != null) {
                fragmentWeakReference.get().loadFail(message, throwable);
            }
        }
    }

    public interface DownloadDialogListener {
        void downloadComplete(String savePath);
    }

}
