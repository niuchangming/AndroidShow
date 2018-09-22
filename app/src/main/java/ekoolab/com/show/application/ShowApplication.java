package ekoolab.com.show.application;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.interceptors.HttpLoggingInterceptor;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.jacksonandroidnetworking.JacksonParserFactory;

public class ShowApplication extends Application {

    public static Application application;

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        AndroidNetworking.initialize(application);
        AndroidNetworking.enableLogging(HttpLoggingInterceptor.Level.BODY);
        Fresco.initialize(this);
    }
}
