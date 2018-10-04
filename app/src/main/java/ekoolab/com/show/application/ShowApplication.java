package ekoolab.com.show.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.androidnetworking.AndroidNetworking;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.luck.picture.lib.utils.AppManager;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.FileReader;

import ekoolab.com.show.BuildConfig;
import ekoolab.com.show.activities.MainActivity;
import ekoolab.com.show.api.FastJsonParserFactory;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.FileUtils;
import ekoolab.com.show.utils.Utils;

public class ShowApplication extends Application implements Thread.UncaughtExceptionHandler {

    public static Application application;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Thread.setDefaultUncaughtExceptionHandler(this);

        Context context = getApplicationContext();
        String packageName = context.getPackageName();
        String processName = getProcessName(android.os.Process.myPid());
        if (processName == null || processName.equals(packageName)) {
            CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
            strategy.setUploadProcess(true);
            CrashReport.initCrashReport(context, "b1505dbce5", BuildConfig.DEBUG, strategy);
            Logger.init("AndroidShow")
                    .methodCount(1)
                    .hideThreadInfo()
                    .logLevel(BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE);
            AndroidNetworking.initialize(application);
            AndroidNetworking.setParserFactory(new FastJsonParserFactory());
            Fresco.initialize(this);
            FileUtils.createOrExistsDir(Constants.VIDEO_PATH);
            FileUtils.createOrExistsDir(Constants.IMAGE_PATH);
            FileUtils.createOrExistsDir(Constants.IMAGE_CACHE_PATH);
            // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
            SDKInitializer.initialize(this);
            //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
            //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
            SDKInitializer.setCoordType(CoordType.BD09LL);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        final Intent intent2 = new Intent();
        String error = Utils.outputError(e);
        AppManager.getInstance().AppExit(this);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent2.setClass(this, MainActivity.class);
        intent2.putExtra(MainActivity.BUNDLE_ERROR_MSG, error);
        startActivity(intent2);
        Process.killProcess(Process.myPid());
    }

    /**
     * 获取进程号对应的进程名
     */
    private static String getProcessName(int pid) {
        try (BufferedReader reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"))) {
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
