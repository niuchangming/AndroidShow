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
import com.faceunity.FURenderer;
import com.luck.picture.lib.utils.AppManager;
import com.orhanobut.logger.LogLevel;
import com.orhanobut.logger.Logger;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.DefaultRefreshFooterCreator;
import com.scwang.smartrefresh.layout.api.DefaultRefreshHeaderCreator;
import com.scwang.smartrefresh.layout.api.RefreshFooter;
import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.sendbird.android.SendBird;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import ekoolab.com.show.BuildConfig;
import ekoolab.com.show.activities.MainActivity;
import ekoolab.com.show.api.FastJsonParserFactory;
import ekoolab.com.show.api.HttpLoggingInterceptor;
import ekoolab.com.show.api.cookie.CookieJarImpl;
import ekoolab.com.show.api.cookie.SPCookieStore;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.FileUtils;
import ekoolab.com.show.utils.Utils;
import okhttp3.OkHttpClient;

public class ShowApplication extends Application implements Thread.UncaughtExceptionHandler {
    public static Application application;
    public static String TEMP_FILE;
    public static IWXAPI iwxapi;

    static {
        //设置全局的Header构建器
        SmartRefreshLayout.setDefaultRefreshHeaderCreator(new DefaultRefreshHeaderCreator() {
            @Override
            public RefreshHeader createRefreshHeader(Context context, RefreshLayout layout) {
                //全局设置主题颜色
//                layout.setPrimaryColorsId(R.color.colorPrimary, android.R.color.white);
                //.setTimeFormat(new DynamicTimeFormat("更新于 %s"));//指定为经典Header，默认是 贝塞尔雷达Header
                return new MaterialHeader(context);
            }
        });
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator(new DefaultRefreshFooterCreator() {
            @Override
            public RefreshFooter createRefreshFooter(Context context, RefreshLayout layout) {
                //指定为经典Footer，默认是 BallPulseFooter
                return new ClassicsFooter(context).setDrawableSize(20);
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        TEMP_FILE = getCacheDir().getAbsolutePath() + "/temp.txt";
        Thread.setDefaultUncaughtExceptionHandler(this);

        Context context = getApplicationContext();
        String packageName = context.getPackageName();
        String processName = getProcessName(android.os.Process.myPid());
        if (processName == null || processName.equals(packageName)) {
            Logger.init("AndroidShow")
                    .methodCount(1)
                    .hideThreadInfo()
                    .logLevel(BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE);
            OkHttpClient okHttpClient = getOkHttpClient();
            AndroidNetworking.initialize(application, okHttpClient);
            AndroidNetworking.setParserFactory(new FastJsonParserFactory());

            Fresco.initialize(this);
            FileUtils.createOrExistsDir(Constants.VIDEO_PATH);
            FileUtils.createOrExistsDir(Constants.IMAGE_PATH);
            FileUtils.createOrExistsDir(Constants.AUDIO_PATH);
            FileUtils.createOrExistsDir(Constants.IMAGE_CACHE_PATH);
            FileUtils.createOrExistsFile(TEMP_FILE);
            // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
//            SDKInitializer.initialize(this);
            //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
            //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
            SDKInitializer.setCoordType(CoordType.BD09LL);

            SendBird.init(Constants.SBD_APP_ID, context);
            FURenderer.initFURenderer(this);

            iwxapi = WXAPIFactory.createWXAPI(this, Constants.WECHAT_APP_ID, false);
            iwxapi.registerApp(Constants.WECHAT_APP_ID);
        }
    }

    public static OkHttpClient getOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("okGo");
        loggingInterceptor.setColorLevel(Level.INFO);
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggingInterceptor);
        builder.cookieJar(new CookieJarImpl(new SPCookieStore(application)));
        //全局的读取超时时间
        builder.readTimeout(60_000, TimeUnit.MILLISECONDS);
        //全局的写入超时时间
        builder.writeTimeout(60_000, TimeUnit.MILLISECONDS);
        //全局的连接超时时间
        builder.connectTimeout(60_000, TimeUnit.MILLISECONDS);
        return builder.build();
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
