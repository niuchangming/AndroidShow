package ekoolab.com.show.application;

import android.app.Application;

import com.androidnetworking.AndroidNetworking;
import com.facebook.FacebookSdk;
import com.jacksonandroidnetworking.JacksonParserFactory;

public class ShowApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        AndroidNetworking.initialize(getApplicationContext());
        AndroidNetworking.setParserFactory(new JacksonParserFactory());
    }
}
