package com.github.ccmagic.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements DownloadFragmentDialog.DownloadDialogListener{

    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.tv).setOnClickListener(v->{
            String downloadUrl="http://www.jikedaohang.com/";
            if (!TextUtils.isEmpty(downloadUrl)) {
                if (downloadUrl.endsWith(".apk")) {
                    UpdateUtil.builder()
                            .setUri(downloadUrl)
                            .setFragmentManager(getSupportFragmentManager())
                            .build();
                } else {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl));
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "shouldOverrideUrlLoading: ", e);
                    }
                }
            } else {
                ToastTool.show("当前链接地址不存在，请重试");
            }
        });
    }

    @Override
    public void downloadComplete(String savePath) {

    }
}
