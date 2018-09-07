package ekoolab.com.show.dialogs;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ProgressView;
import org.json.JSONException;
import org.json.JSONObject;
import ekoolab.com.show.R;
import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.utils.Constants;

public class VerifyDialog extends SimpleDialog implements View.OnClickListener{
    private final String TAG = "VerifyDialog";
    private EditText codeEt;
    private Button cancelBtn;
    private Button verifyBtn;
    private ProgressView loadingBar;
    private AuthInfo authInfo;
    private CountDownTimer timer;
    private VerifyListener listener;

    public VerifyDialog(Context context) {
        this(context, R.style.SimpleDialogLight);
    }

    public VerifyDialog(Context context, int style) {
        super(context, style);
        contentView(R.layout.dialog_verify2fa);
        initViews();
    }

    public void setAuthInfo(AuthInfo authInfo) {
        this.authInfo = authInfo;
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

        beforeCall();
        AndroidNetworking.post(Constants.VERIFY_2FA)
                .addBodyParameter("verifyCode", code)
                .addBodyParameter("token", authInfo.apiToken)
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
                                AuthInfo info = new AuthInfo(data);
                                authInfo.userCode = info.userCode;
                                authInfo.apiToken = info.apiToken;
                                authInfo.accountType = "mobile";

                                if(listener != null){
                                    listener.did2FAVerify(info);
                                }

                                cancel();
                            } else {
                                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                            }
                        }catch (JSONException e){
                            Log.e(TAG, e.getLocalizedMessage());
                        }

                        afterCall();
                    }
                    @Override
                    public void onError(ANError error) {
                        Log.e(TAG, error.getLocalizedMessage());
                        afterCall();
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
        void did2FAVerify(AuthInfo authInfo);
    }
}
