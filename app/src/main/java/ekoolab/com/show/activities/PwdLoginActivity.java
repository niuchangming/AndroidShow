package ekoolab.com.show.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

public class PwdLoginActivity extends BaseActivity implements View.OnClickListener,
        RegisterDialog.RegisterListener, VerifyDialog.VerifyListener,
        ForgetPasswordDialog.VerifyMobileListener, NewPasswordDialog.ChangePasswordListener {
    private final static String TAG = "SMSLoginActivity";
    public static final String LOGIN_DATA = "login_data";
    private EditText mobileEt;
    private EditText passwordEt;
    private Button loginBtn;
    private Button forgetPwdBtn;

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
                    PwdLoginActivity.this.finish();
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
        return R.layout.activity_pwd_login;
    }

    @Override
    protected void initViews() {
        super.initViews();

        mobileEt = findViewById(R.id.log_mobile_et);
        passwordEt = findViewById(R.id.password_et);

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
                toastLong(PwdLoginActivity.this.getString(R.string.login_cancel));
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
            case R.id.forget_pwd_btn:
                Intent pwdLoginIntent = new Intent(this, PwdLoginActivity.class);
                this.startActivity(pwdLoginIntent);
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

        beforeLogin(LoginType.MOBILE);
        HashMap<String, String> map = new HashMap<>(4);
        map.put("countryCode", "65");
        map.put("mobile", mobile);
        map.put("password", password);
        map.put("type", "mobile");
        ApiServer.basePostRequest(this, Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>(){})
                .subscribe(new NetworkSubscriber<LoginData>() {
                    @Override
                    protected void onSuccess(LoginData loginData) {
                        afterLogin(LoginType.MOBILE);
                        AuthUtils.getInstance(getApplicationContext()).saveLoginInfo(loginData);
                        broadcastLoggedIn(loginData);
                        PwdLoginActivity.this.finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        afterLogin(LoginType.MOBILE);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void facebookLogin() {
        fbLoginManager.logInWithReadPermissions(PwdLoginActivity.this, Arrays.asList("public_profile"));
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
                        PwdLoginActivity.this.finish();
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
        super.onDestroy();
    }

}
