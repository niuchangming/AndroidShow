package ekoolab.com.show.utils;

import android.content.Context;
import android.os.Looper;
import android.widget.Toast;


import ekoolab.com.show.application.ShowApplication;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2017/07/31
 * @description 显示Toast
 */
public class ToastUtils {

    private static Toast toast;

    private static void createToast(Context context, String content, int resId) {
        if (context == null) {
            return;
        }
        if (toast == null) {
            //防止内存泄漏
            Context applicationContext = context.getApplicationContext();
            if (resId > 0) {
                toast = Toast.makeText(applicationContext, resId, Toast.LENGTH_SHORT);
            } else {
                toast = Toast.makeText(applicationContext, content, Toast.LENGTH_SHORT);
            }
        }
        toast.setDuration(Toast.LENGTH_SHORT);
        if (resId > 0) {
            toast.setText(resId);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

    public static void showToast(final String content) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            createToast(ShowApplication.application, content, 0);
        } else {
            UIHandler.getInstance().post(() -> createToast(ShowApplication.application, content, 0));
        }
    }

    public static void showToast(final int resId) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            createToast(ShowApplication.application, "", resId);
        } else {
            UIHandler.getInstance().post(() -> createToast(ShowApplication.application, "", resId));
        }
    }
}
