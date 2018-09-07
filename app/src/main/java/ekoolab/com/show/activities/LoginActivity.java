package ekoolab.com.show.activities;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.rey.material.widget.ProgressView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.dialogs.RegisterDialog;
import ekoolab.com.show.dialogs.VerifyDialog;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;

public class LoginActivity extends BaseActivity implements View.OnClickListener,
        RegisterDialog.RegisterListener, VerifyDialog.VerifyListener{
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
            public void onCompletion(MediaPlayer mp) {
                videoView.start();
            }
        });
        videoView.start();
    }

    private void initFacebookStuff(){
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
        switch(view.getId()){
            case R.id.login_btn:
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

    private void mobileLogin(){
        final String mobile = mobileEt.getText().toString().trim();
        final String password = passwordEt.getText().toString().trim();

        beforeLogin(false);
        AndroidNetworking.post(Constants.LOGIN)
                .addBodyParameter("countryCode", "65")
                .addBodyParameter("mobile", mobile)
                .addBodyParameter("password", password)
                .addBodyParameter("type", "mobile")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            int errorCode = response.getInt("errorCode");
                            String message = response.getString("message");
                            if (errorCode == 1) {
                                JSONObject data = response.getJSONObject("data");
                                AuthInfo authInfo = new AuthInfo(data);
                                authInfo.setMobile(mobile);
                                authInfo.setDialNo("65");
//                                authInfo.mobile = mobile;
//                                authInfo.dialNo = "65";
                                AuthUtils.getInstance(LoginActivity.this).saveAuthInfo(authInfo);
                                LoginActivity.this.finish();
                            } else {
                                toastLong(message);
                            }
                        }catch (JSONException e){
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                        afterLogin(true);
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, error.getLocalizedMessage());
                        afterLogin(true);
                    }
                });
    }

    private void facebookLogin(){
        fbLoginManager.logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile"));

    }

    private void afterFacebookLogin(final LoginResult loginResult){

        beforeLogin(true);
        AndroidNetworking.post(Constants.LOGIN)
                .addBodyParameter("countryCode", "65")
                .addBodyParameter("fb_token", loginResult.getAccessToken().getToken())
                .addBodyParameter("expired", loginResult.getAccessToken().getExpires().getTime() + "")
                .addBodyParameter("fb_id", loginResult.getAccessToken().getUserId())
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            int errorCode = response.getInt("errorCode");
                            String message = response.getString("message");
                            if (errorCode == 1) {
                                JSONObject data = response.getJSONObject("data");
                                AuthInfo authInfo = new AuthInfo(data);
                                authInfo.setFbAccessToken(loginResult.getAccessToken().getToken());
                                //authInfo.fbAccessToken = loginResult.getAccessToken().getToken();
                                authInfo.setFbExpiredDate(loginResult.getAccessToken().getExpires());
                                //authInfo.fbExpiredDate = loginResult.getAccessToken().getExpires();
                                authInfo.setFbUserId(loginResult.getAccessToken().getUserId());
                                //authInfo.fbUserId = loginResult.getAccessToken().getUserId();
                                AuthUtils.getInstance(LoginActivity.this).saveAuthInfo(authInfo);
                                LoginActivity.this.finish();
                            } else {
                                toastLong(message);
                            }
                        }catch (JSONException e){
                            Log.e(TAG, e.getLocalizedMessage());
                        }

                        afterLogin(true);
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, error.getLocalizedMessage());
                        afterLogin(true);
                    }
                });
    }

    private void beforeLogin(boolean isFB){
        if(isFB){
            fbLoginLoadingBar.start();
            facebookBtn.setVisibility(View.INVISIBLE);
            loginBtn.setEnabled(false);
        }else{
            loginLoading.start();
            facebookBtn.setEnabled(false);
            loginBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void afterLogin(boolean isFB){
        if(isFB){
            fbLoginLoadingBar.stop();
            facebookBtn.setVisibility(View.VISIBLE);
            loginBtn.setEnabled(true);
        }else{
            loginLoading.stop();
            facebookBtn.setEnabled(true);
            loginBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void didRegistered(AuthInfo authInfo) {
        VerifyDialog verifyDialog = new VerifyDialog(this);
        verifyDialog.setAuthInfo(authInfo);
        verifyDialog.backgroundColor(this.getResources().getColor(R.color.colorPink));
        verifyDialog.show();
    }

    @Override
    public void did2FAVerify(AuthInfo authInfo) {
        if(authInfo != null){
            AuthUtils.getInstance(this).saveAuthInfo(authInfo);
        }
        finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
