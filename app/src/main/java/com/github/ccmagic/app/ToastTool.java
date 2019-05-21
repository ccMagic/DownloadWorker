package com.github.ccmagic.app;

import android.app.Application;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;

import com.hjq.toast.IToastStyle;
import com.hjq.toast.ToastUtils;

/**
 * @author kxmc
 * 2019/3/7 18:24
 */
public class ToastTool {

    /**
     * 如果对Toast的默认样式不满意，可以在Application初始化样式，具体可参考ToastBlackStyle类的实现
     * 统一全局的Toast样式，建议在{@link Application#onCreate()}中初始化
     *
     * @param style 样式实现类，框架已经实现三种不同的样式
     *              黑色样式：ToastBlackStyle
     *              白色样式：ToastWhiteStyle
     *              仿QQ样式：ToastQQStyle
     */
    public static void init(Application application,IToastStyle style) {
        ToastUtils.init(application,style);
    }


    public static void show(String message) {
        ToastUtils.show(message);
    }

    public static void show(@StringRes int id) {
        ToastUtils.show(id);
    }


    public static <T> void show(T t) {
        ToastUtils.show(t);
    }

    /**
     * 给当前Toast设置新的布局
     */
    public static void setView(View view) {
        ToastUtils.setView(view);
    }

    public static void setView(@LayoutRes int layoutId) {
        ToastUtils.setView(layoutId);
    }

    /**
     * 设置吐司的位置
     *
     * @param gravity 重心
     * @param xOffset x轴偏移
     * @param yOffset y轴偏移
     */
    public static void setGravity(int gravity, int xOffset, int yOffset) {
        ToastUtils.setGravity(gravity, xOffset, yOffset);
    }

    /**
     * 获取当前Toast对象
     */
    public static Toast getToast() {
        return ToastUtils.getToast();
    }
}
