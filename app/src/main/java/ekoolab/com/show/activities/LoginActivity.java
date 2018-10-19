package ekoolab.com.show.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.VideoView;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.reflect.TypeToken;
import com.rey.material.widget.ProgressView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.dialogs.RegisterDialog;
import ekoolab.com.show.dialogs.VerifyDialog;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.Utils;

public class LoginActivity extends BaseActivity implements View.OnClickListener,
        RegisterDialog.RegisterListener, VerifyDialog.VerifyListener {
    private final static String TAG = "LoginActivity";
    private VideoView videoView;
    private String videoPath;
    private EditText mobileEt;
    private EditText passwordEt;
    private Button loginBtn;
    private Button registerBtn;

    private CallbackManager facebookCallbackManager;
    private LoginManager fbLoginManager;
    private ImageButton facebookBtn;
    private ProgressView fbLoginLoadingBar;
    private ProgressView loginLoading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        initFacebookStuff();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        super.initData();
        videoPath = "android.resource://" + getPackageName() + "/" + R.raw.login_vd;
    }

    @Override
    protected void initViews() {
        super.initViews();

        mobileEt = findViewById(R.id.log_mobile_et);
        passwordEt = findViewById(R.id.log_password_et);

        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);

        registerBtn = findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(this);

        facebookBtn = findViewById(R.id.fb_login_btn);
        facebookBtn.setOnClickListener(this);

        fbLoginLoadingBar = findViewById(R.id.fb_login_pv);
        loginLoading = findViewById(R.id.login_pv);

        videoView = findViewById(R.id.login_video_view);
        videoView.setVideoURI(Uri.parse(videoPath));
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoView.start();
    }

    private void initFacebookStuff() {
        fbLoginManager = LoginManager.getInstance();
        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                afterFacebookLogin(loginResult);
            }

            @Override
            public void onCancel() {
                toastLong(LoginActivity.this.getString(R.string.login_cancel));
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, error.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_btn:
                Utils.hideInput(passwordEt);
                mobileLogin();
                break;
            case R.id.register_btn:
                RegisterDialog dialog = new RegisterDialog(this);
                dialog.backgroundColor(getResources().getColor(R.color.colorPink));
                dialog.show();
                break;
            case R.id.fb_login_btn:
                facebookLogin();
                break;
        }
    }

    private void mobileLogin() {
        final String mobile = mobileEt.getText().toString().trim();
        final String password = passwordEt.getText().toString().trim();

        if (Utils.isBlank(mobile)) {
            mobileEt.setHint(R.string.mobile_hint);
            mobileEt.setHintTextColor(ContextCompat.getColor(this, R.color.colorRed));
            return;
        }

        if(Utils.isBlank(password)){
            passwordEt.setHint(R.string.password_hint);
            passwordEt.setHintTextColor(ContextCompat.getColor(this, R.color.colorRed));
            return;
        }

        beforeLogin(false);
        HashMap<String, String> map = new HashMap<>(4);
        map.put("countryCode", "65");
        map.put("mobile", mobile);
        map.put("password", password);
        map.put("type", "mobile");
        ApiServer.basePostRequest(this, Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>(){})
                .subscribe(new NetworkSubscriber<LoginData>() {
                    @Override
                    protected void onSuccess(LoginData loginData) {
                        afterLogin(false);
                        AuthUtils.getInstance(getApplicationContext()).saveLoginInfo(loginData);
                        broadcastLoggedIn();
                        LoginActivity.this.finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        afterLogin(false);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void facebookLogin() {
        fbLoginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));
    }

    private void afterFacebookLogin(final LoginResult loginResult) {
        beforeLogin(true);
        HashMap<String, String> map = new HashMap<>(4);
        map.put("type", "facebook");
        map.put("fb_token", loginResult.getAccessToken().getToken());
        map.put("expired", loginResult.getAccessToken().getExpires().getTime() + "");
        map.put("fb_id", loginResult.getAccessToken().getUserId());
        ApiServer.basePostRequest(this, Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>(){})
                .subscribe(new NetworkSubscriber<LoginData>() {
                    @Override
                    protected void onSuccess(LoginData loginData) {
                        afterLogin(true);
                        broadcastLoggedIn();
                        AuthUtils.getInstance(getApplicationContext()).saveLoginInfo(loginData);
                        LoginActivity.this.finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        afterLogin(true);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void beforeLogin(boolean isFB) {
        if (isFB) {
            fbLoginLoadingBar.start();
            facebookBtn.setVisibility(View.INVISIBLE);
            loginBtn.setEnabled(false);
        } else {
            loginLoading.start();
            facebookBtn.setEnabled(false);
            loginBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void afterLogin(boolean isFB) {
        if (isFB) {
            fbLoginLoadingBar.stop();
            facebookBtn.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(true);
        } else {
            loginLoading.stop();
            facebookBtn.setEnabled(true);
            loginBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void didRegistered(LoginData loginData) {
        VerifyDialog verifyDialog = new VerifyDialog(this);
        verifyDialog.setLoginData(loginData);
        verifyDialog.backgroundColor(this.getResources().getColor(R.color.colorPink));
        verifyDialog.show();
    }

    @Override
    public void did2FAVerify(LoginData loginData) {
        if (loginData != null) {
            AuthUtils.getInstance(this).saveLoginInfo(loginData);
            broadcastLoggedIn();
        }
        finish();
    }

    public void broadcastLoggedIn(){
        Intent msgIntent = new Intent();
        msgIntent.setAction(AuthUtils.LOGGED_IN);
        this.sendBroadcast(msgIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
