package ekoolab.com.show.dialogs;

import com.google.gson.reflect.TypeToken;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ProgressView;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.Utils;

public class ForgetPasswordDialog extends SimpleDialog implements View.OnClickListener {

    private EditText mobile_et;
    private EditText verify_code_et;
    private TextView send_sms_tv;
    private ProgressView loadingBarSend;
    private Button cancel_btn;
    private Button verify_btn;
    private CountDownTimer timer;
    private ForgetPasswordDialog.VerifyMobileListener listener;
    private String mobile, token;
    private LoginData loginData;

    public ForgetPasswordDialog(Context context) {
        this(context, R.style.SimpleDialogLight);
    }

    public ForgetPasswordDialog(Context context, int style) {
        super(context, style);
        contentView(R.layout.dialog_forget_password);

        if(ForgetPasswordDialog.VerifyMobileListener.class.isAssignableFrom(context.getClass())){
            listener = (ForgetPasswordDialog.VerifyMobileListener)context;
        }

        initViews();
    }

    private void initViews(){
        loadingBarSend = findViewById(R.id.send_pv);
        mobile_et = findViewById(R.id.mobile_et);
        verify_code_et = findViewById(R.id.verify_code_et);
        send_sms_tv = findViewById(R.id.send_sms_tv);
        send_sms_tv.setOnClickListener(this);

        cancel_btn = findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(this);

        verify_btn = findViewById(R.id.verify_btn);
//        verify_btn.setOnClickListener(this);
//        verify_btn.setEnabled(false);

//        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN){
//
//                }
//                return true;
//            }
//        };
//
//        mobile_et.setOnEditorActionListener(enterListener);
//        verify_code_et.setOnEditorActionListener(enterListener);
    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.cancel_btn:
                cancel();
                break;
            case R.id.send_sms_tv:
                sendsms();
                break;
            case R.id.verify_btn:
                System.out.println("choose to verify");
                verify();
                break;
        }

    }

    private void startTimer(){
        if(timer == null) {
            timer = new CountDownTimer(60 * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    send_sms_tv.setText(Long.toString(millisUntilFinished / 1000));
                }

                public void onFinish() {
                    send_sms_tv.setText(getContext().getString(R.string.resend));
                }
            };
        }
        timer.start();
    }

    private void verify(){
        String mobile = mobile_et.getText().toString().trim();
        String verify_code = verify_code_et.getText().toString().trim();

        if (Utils.isBlank(mobile) || loginData == null || !mobile.equals(loginData.mobile)) {
            mobile_et.setHint(R.string.mobile_hint);
            mobile_et.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        if(Utils.isBlank(verify_code)){
            verify_code_et.setHint(R.string.password_hint);
            verify_code_et.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        beforeVerify();

        HashMap<String, Object> map = new HashMap<>();
        map.put("sms", verify_code);
        map.put("token", loginData.token);

        ApiServer.basePostRequestNoDisposable(Constants.VERIFY_2FA_V2, map, new TypeToken<ResponseData<LoginData>>() {
        }).subscribe(new NetworkSubscriber<LoginData>() {
            @Override
            protected void onSuccess(LoginData loginData) {
                if (listener != null){
                    loginData.mobile = mobile;
                    listener.didVerifyMobile(loginData);
                }
                afterVerify();
                cancel();
            }

            @Override
            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                afterVerify();
                return super.dealHttpException(code, errorMsg, e);
            }

        });

    }




    private void sendsms(){
        startTimer();
        final String mobile = mobile_et.getText().toString().trim();

        if (Utils.isBlank(mobile)) {
            mobile_et.setHint(R.string.mobile_hint);
            mobile_et.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        beforeSend();

        HashMap<String, Object> map = new HashMap<>();
//        map.put("resetpwd", "?resetpwd=true");
//        map.put("countryCode", "65");
        map.put("mobile", String.valueOf(mobile));
//        map.put("type", "mobile");

        ApiServer.basePostRequestNoDisposable(Constants.SIGNUP_V2 + "?resetpwd=true", map, new TypeToken<ResponseData<LoginData>>() {
        }).subscribe(new NetworkSubscriber<LoginData>() {
            @Override
            protected void onSuccess(LoginData loginData) {
                loginData.mobile = mobile;
                verify_btn.setOnClickListener(ForgetPasswordDialog.this);
                afterSend(loginData);
            }

            @Override
            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                afterSend(new LoginData());
                return super.dealHttpException(code, errorMsg, e);
            }

        });
    }

    private void beforeSend(){
        send_sms_tv.setVisibility(View.INVISIBLE);
        loadingBarSend.start();
    }

    private void afterSend(LoginData loginData){
        this.loginData = loginData;
        send_sms_tv.setVisibility(View.VISIBLE);
        loadingBarSend.stop();
    }

    private void beforeVerify(){
        verify_btn.setVisibility(View.INVISIBLE);
    }

    private void afterVerify(){
        verify_btn.setVisibility(View.VISIBLE);
    }

    public interface VerifyMobileListener{
        void didVerifyMobile(LoginData loginData);
    }
}
