package com.xtc.lint;

import android.content.Context;
import android.widget.Toast;

/**
 * Toast工具类
 *<p/>
 *created by OuyangPeng on 2017/1/5.
 */
public class ToastUtils {
    /**
     * 展示文本
     * @param context 上下文
     * @param toastString 要展示的文本
     */
    public static void showToast(Context context ,String toastString) {
        Toast.makeText(context,toastString,Toast.LENGTH_SHORT).show();
    }
}
