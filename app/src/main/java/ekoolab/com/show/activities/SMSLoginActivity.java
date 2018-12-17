package ekoolab.com.show.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.reflect.TypeToken;
import com.rey.material.widget.ProgressView;
import com.tencent.mm.opensdk.modelmsg.SendAuth;

import java.util.Arrays;
import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.application.ShowApplication;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.beans.enums.LoginType;
import ekoolab.com.show.dialogs.ForgetPasswordDialog;
import ekoolab.com.show.dialogs.NewPasswordDialog;
import ekoolab.com.show.dialogs.RegisterDialog;
import ekoolab.com.show.dialogs.VerifyDialog;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.wxapi.WXEntryActivity;

public class SMSLoginActivity extends BaseActivity implements View.OnClickListener,
        RegisterDialog.RegisterListener, VerifyDialog.VerifyListener,
        ForgetPasswordDialog.VerifyMobileListener, NewPasswordDialog.ChangePasswordListener {
    private final static String TAG = "SMSLoginActivity";
    public static final String LOGIN_DATA = "login_data";
    private EditText mobileEt;
    private EditText verifyCodeEt;
    private Button loginBtn;
    private Button forgetPwdBtn;
    private Button requestCodeBtn;
    private ProgressView requestCodeLoading;
    private CountDownTimer timer;

    private CallbackManager facebookCallbackManager;
    private LoginManager fbLoginManager;
    private ImageButton facebookBtn;
    private ProgressView fbLoginLoadingBar;
    private ImageButton wxLoginBtn;
    private ProgressView wxLoginLoadingBar;
    private ProgressView loginLoading;

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WXEntryActivity.WX_LOGIN_STARTED)) {
                beforeLogin(LoginType.WECHAT);
            }else if (intent.getAction().equals(WXEntryActivity.WX_LOGIN_ENDED)) {
                LoginData loginData = intent.getParcelableExtra(LOGIN_DATA);
                afterLogin(LoginType.WECHAT);

                if(loginData != null){
                    broadcastLoggedIn(loginData);
                    SMSLoginActivity.this.finish();
                }
            }
        }
    };

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
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WXEntryActivity.WX_LOGIN_STARTED);
        filter.addAction(WXEntryActivity.WX_LOGIN_ENDED);
        this.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initViews() {
        super.initViews();

        mobileEt = findViewById(R.id.log_mobile_et);
        verifyCodeEt = findViewById(R.id.verify_code_et);

        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);

        facebookBtn = findViewById(R.id.fb_login_btn);
        facebookBtn.setOnClickListener(this);

        wxLoginBtn = findViewById(R.id.wx_login_btn);
        wxLoginBtn.setOnClickListener(this);

        wxLoginLoadingBar = findViewById(R.id.wx_login_pv);
        fbLoginLoadingBar = findViewById(R.id.fb_login_pv);
        loginLoading = findViewById(R.id.login_pv);

        forgetPwdBtn = findViewById(R.id.forget_pwd_btn);
        forgetPwdBtn.setOnClickListener(this);

        requestCodeBtn = findViewById(R.id.request_code_btn);
        requestCodeBtn.setOnClickListener(this);

        requestCodeLoading = findViewById(R.id.request_code_pv);
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
                toastLong(SMSLoginActivity.this.getString(R.string.login_cancel));
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
                Utils.hideInput(verifyCodeEt);
                mobileLogin();
                break;
            case R.id.forget_pwd_btn:

                break;
            case R.id.register_btn:
                RegisterDialog dialog = new RegisterDialog(this);
                dialog.backgroundColor(getResources().getColor(R.color.colorPink));
                dialog.show();
                break;
            case R.id.fb_login_btn:
                facebookLogin();
                break;
            case R.id.wx_login_btn:
                wxLogin();
                break;
            case R.id.request_code_btn:
                String label = requestCodeBtn.getText().toString().trim();
                if(label.equalsIgnoreCase("resend")){
                    startTimer();
                }else{
                    requestCode();
                }
                break;
        }
    }

    private void startTimer(){
        if(timer == null) {
            timer = new CountDownTimer(60 * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    requestCodeBtn.setText(getString(R.string.verify_count_down, "" + millisUntilFinished / 1000));
                }

                public void onFinish() {
                    requestCodeBtn.setText(getString(R.string.resend));
                    requestCodeBtn.setEnabled(true);
                }
            };
        }
        timer.start();
    }

    private void stopTimer(){
        if(timer != null){
            timer.onFinish();
            timer = null;
        }
    }

    private void requestCode(){
        final String mobile = mobileEt.getText().toString().trim();

        if (Utils.isBlank(mobile)) {
            mobileEt.setHint(R.string.mobile_hint);
            mobileEt.setHintTextColor(ContextCompat.getColor(this, R.color.colorRed));
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("countryCode", "65");
        map.put("mobile", mobile);
        map.put("type", "sms");

        requestCodeLoading.start();
        requestCodeBtn.setText("");
        requestCodeBtn.setEnabled(false);
        ApiServer.basePostRequestNoDisposable(Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>() {
        }).subscribe(new NetworkSubscriber<LoginData>() {
            @Override
            protected void onSuccess(LoginData loginData) {
                startTimer();
                requestCodeLoading.stop();
                requestCodeBtn.setText(getString(R.string.request_code));
                requestCodeBtn.setEnabled(false);
            }

            @Override
            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                requestCodeLoading.stop();
                requestCodeBtn.setText(getString(R.string.request_code));
                requestCodeBtn.setEnabled(true);
                return super.dealHttpException(code, errorMsg, e);
            }

        });

    }

    private void mobileLogin() {
        final String mobile = mobileEt.getText().toString().trim();
        final String sms = verifyCodeEt.getText().toString().trim();

        if (Utils.isBlank(mobile)) {
            mobileEt.setHint(R.string.mobile_hint);
            mobileEt.setHintTextColor(ContextCompat.getColor(this, R.color.colorRed));
            return;
        }

        if(Utils.isBlank(sms)){
            verifyCodeEt.setHint(R.string.verify_hint);
            verifyCodeEt.setHintTextColor(ContextCompat.getColor(this, R.color.colorRed));
            return;
        }

        beforeLogin(LoginType.MOBILE);
        HashMap<String, String> map = new HashMap<>(4);
        map.put("countryCode", "65");
        map.put("mobile", mobile);
        map.put("sms", sms);
        map.put("type", "2fa");
        ApiServer.basePostRequest(this, Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>(){})
                .subscribe(new NetworkSubscriber<LoginData>() {
                    @Override
                    protected void onSuccess(LoginData loginData) {
                        afterLogin(LoginType.MOBILE);
                        AuthUtils.getInstance(getApplicationContext()).saveLoginInfo(loginData);
                        broadcastLoggedIn(loginData);
                        SMSLoginActivity.this.finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        afterLogin(LoginType.MOBILE);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void facebookLogin() {
        fbLoginManager.logInWithReadPermissions(SMSLoginActivity.this, Arrays.asList("public_profile"));
    }

    private void afterFacebookLogin(final LoginResult loginResult) {
        beforeLogin(LoginType.FACEBOOK);
        HashMap<String, String> map = new HashMap<>(4);
        map.put("type", "facebook");
        map.put("fb_token", loginResult.getAccessToken().getToken());
        map.put("expired", loginResult.getAccessToken().getExpires().getTime() + "");
        map.put("fb_id", loginResult.getAccessToken().getUserId());
        ApiServer.basePostRequest(this, Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>(){})
                .subscribe(new NetworkSubscriber<LoginData>() {
                    @Override
                    protected void onSuccess(LoginData loginData) {
                        afterLogin(LoginType.FACEBOOK);
                        AuthUtils.getInstance(getApplicationContext()).saveLoginInfo(loginData);
                        broadcastLoggedIn(loginData);
                        SMSLoginActivity.this.finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        afterLogin(LoginType.FACEBOOK);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void wxLogin(){
        if (!ShowApplication.iwxapi.isWXAppInstalled()) {
            toastLong(getString(R.string.no_wx_install));
            return;
        }
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "diandi_wx_login";
        ShowApplication.iwxapi.sendReq(req);
    }

    private void beforeLogin(LoginType loginType) {
        switch (loginType){
            case MOBILE:
                loginLoading.start();
                loginBtn.setVisibility(View.INVISIBLE);
                break;
            case WECHAT:
                wxLoginLoadingBar.start();
                wxLoginBtn.setVisibility(View.INVISIBLE);
                break;
            case FACEBOOK:
                fbLoginLoadingBar.start();
                facebookBtn.setVisibility(View.INVISIBLE);
                break;
        }
        loginBtn.setEnabled(false);
        wxLoginBtn.setEnabled(false);
        facebookBtn.setEnabled(false);
    }

    private void afterLogin(LoginType loginType) {
        switch (loginType){
            case MOBILE:
                loginLoading.stop();
                loginBtn.setVisibility(View.VISIBLE);
                break;
            case WECHAT:
                wxLoginLoadingBar.stop();
                wxLoginBtn.setVisibility(View.VISIBLE);
                break;
            case FACEBOOK:
                fbLoginLoadingBar.stop();
                facebookBtn.setVisibility(View.VISIBLE);
                break;
        }
        facebookBtn.setEnabled(true);
        loginBtn.setEnabled(true);
        wxLoginBtn.setEnabled(true);
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
            broadcastLoggedIn(loginData);
        }
        finish();
    }

    @Override
    public void didVerifyMobile(LoginData loginData) {
        NewPasswordDialog newPasswordDialog = new NewPasswordDialog(this);
        newPasswordDialog.setLoginData(loginData);
        newPasswordDialog.backgroundColor(this.getResources().getColor(R.color.colorPink));
        newPasswordDialog.show();
    }

    @Override
    public void didChangePassword(LoginData loginData) {
        if (loginData != null) {
            AuthUtils.getInstance(this).saveLoginInfo(loginData);
            broadcastLoggedIn(loginData);
        }
        finish();
    }

    public void broadcastLoggedIn(LoginData loginData){
        Intent msgIntent = new Intent();
        msgIntent.setAction(AuthUtils.LOGGED_IN);
        msgIntent.putExtra(LOGIN_DATA, loginData);
        this.sendBroadcast(msgIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        stopTimer();
        super.onDestroy();
    }

}
