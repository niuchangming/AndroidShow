package ekoolab.com.show.application;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.androidnetworking.AndroidNetworking;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.facebook.drawee.backends.pipeline.Fresco;

import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.FileUtils;

public class ShowApplication extends Application {

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
        AndroidNetworking.initialize(application);
        Fresco.initialize(this);
        FileUtils.createOrExistsDir(Constants.VIDEO_PATH);
        FileUtils.createOrExistsDir(Constants.IMAGE_PATH);
        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(this);
        //自4.3.0起，百度地图SDK所有接口均支持百度坐标和国测局坐标，用此方法设置您使用的坐标类型.
        //包括BD09LL和GCJ02两种坐标，默认是BD09LL坐标。
        SDKInitializer.setCoordType(CoordType.BD09LL);
    }
}
