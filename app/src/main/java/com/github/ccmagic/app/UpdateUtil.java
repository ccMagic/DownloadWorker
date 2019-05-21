package com.github.ccmagic.app;

import androidx.fragment.app.FragmentManager;


/**
 * @author kxmc
 * 2019/3/8 19:52
 */
public class UpdateUtil {


    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private FragmentManager fragmentManager;
        private String uri;
        private String fileName;
        private String savePath;

        public Builder setFragmentManager(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            return this;
        }

        public Builder setUri(String uri) {
            this.uri = uri;
            return this;
        }

        public Builder setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public Builder setSavePath(String savePath) {
            this.savePath = savePath;
            return this;
        }

        public void build() {
            DownloadFragmentDialog downloadFragment = DownloadFragmentDialog.newInstance(uri, fileName, savePath);
            downloadFragment.show(fragmentManager, "dialog");
        }
    }
}
