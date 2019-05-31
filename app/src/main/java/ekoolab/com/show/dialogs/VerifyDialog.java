package ekoolab.com.show.dialogs;

import android.content.Context;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ProgressView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.JsonParser.JSONParser;
import ekoolab.com.show.utils.Utils;

public class VerifyDialog extends SimpleDialog implements View.OnClickListener{
    private final String TAG = "VerifyDialog";
    private EditText codeEt;
    private Button cancelBtn;
    private Button verifyBtn;
    private ProgressView loadingBar;
    private LoginData loginData;
    private CountDownTimer timer;
    private VerifyListener listener;

    public VerifyDialog(Context context) {
        this(context, R.style.SimpleDialogLight);
    }

    public VerifyDialog(Context context, int style) {
        super(context, style);
        contentView(R.layout.dialog_verify2fa);

        if(VerifyListener.class.isAssignableFrom(context.getClass())){
            listener = (VerifyListener)context;
        }

        initViews();
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
    }

    @Override
    public void show() {
        super.show();
        startTimer();
    }

    private void initViews(){
        codeEt = findViewById(R.id.verify_code_et);

        cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(this);

        verifyBtn = findViewById(R.id.verify_btn);
        verifyBtn.setOnClickListener(this);

        loadingBar = findViewById(R.id.verify_pv);
    }

    private void startTimer(){
        if(timer == null) {
            timer = new CountDownTimer(60 * 1000, 1000) {

                public void onTick(long millisUntilFinished) {
                    verifyBtn.setText(getContext().getString(R.string.verify_count_down, "" + millisUntilFinished / 1000));
                }

                public void onFinish() {
                    verifyBtn.setText(getContext().getString(R.string.resend));
                }
            };
        }
        timer.start();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.verify_btn:
                String label = verifyBtn.getText().toString().trim();
                if(label.equalsIgnoreCase("resend")){
                    startTimer();
                }else{
                    verify();
                }
                break;
            case R.id.cancel_btn:
                cancel();
                break;
        }
    }

    private void verify(){
        final String code = codeEt.getText().toString().trim();

        if (Utils.isBlank(code)) {
            codeEt.setHint(R.string.mobile_hint);
            codeEt.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        beforeCall();

        HashMap<String, Object> map = new HashMap<>();
        map.put("verifyCode", code);
        map.put("token", loginData.token);

        ApiServer.basePostRequestNoDisposable(Constants.VERIFY_2FA, map, new TypeToken<ResponseData<LoginData>>() {
        }).subscribe(new NetworkSubscriber<LoginData>() {
            @Override
            protected void onSuccess(LoginData loginData) {
                if (listener != null){
                    listener.did2FAVerify(loginData);
                }
                afterCall();
                cancel();
            }

            @Override
            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                afterCall();
                return super.dealHttpException(code, errorMsg, e);
            }
        });

    }

    @Override
    public void hide() {
        super.hide();
        if(timer == null) {
            stopTimer();
        }
    }

    private void stopTimer(){
        timer.onFinish();
        timer = null;
    }

    private void beforeCall(){
        verifyBtn.setVisibility(View.INVISIBLE);
        loadingBar.start();
    }

    private void afterCall(){
        verifyBtn.setVisibility(View.VISIBLE);
        loadingBar.stop();
    }

    public interface VerifyListener{
        void did2FAVerify(LoginData loginData);
    }
}
