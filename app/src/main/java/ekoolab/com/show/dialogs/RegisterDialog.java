package ekoolab.com.show.dialogs;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.reflect.TypeToken;
import com.luck.picture.lib.tools.Constant;
import com.orhanobut.logger.Logger;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ProgressView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.AuthInfo;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.beans.Moment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ListUtils;
import ekoolab.com.show.utils.Utils;

public class RegisterDialog extends SimpleDialog implements View.OnClickListener{
    private final String TAG = "RegisterDialog";;

    private EditText mobileEt;
    private EditText passwordEt;
    private ProgressView loadingBar;
    private Button cancelBtn;
    private Button registerBtn;
    private RegisterListener listener;

    public RegisterDialog(Context context) {
        this(context, R.style.SimpleDialogLight);
    }

    public RegisterDialog(Context context, int style) {
        super(context, style);
        contentView(R.layout.dialog_register);

        if(RegisterListener.class.isAssignableFrom(context.getClass())){
            listener = (RegisterListener)context;
        }

        initViews();
    }

    private void initViews(){
        loadingBar = findViewById(R.id.reg_pv);
        mobileEt = findViewById(R.id.reg_mobile_et);
        passwordEt = findViewById(R.id.reg_password_et);

        cancelBtn = findViewById(R.id.reg_cancel);
        cancelBtn.setOnClickListener(this);

        registerBtn = findViewById(R.id.register_btn);
        registerBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.reg_cancel:
                cancel();
                break;
            case R.id.register_btn:
                register();
                break;
        }

    }

    private void register(){
        final String mobile = mobileEt.getText().toString().trim();
        final String password = passwordEt.getText().toString().trim();

        if (Utils.isBlank(mobile)) {
            mobileEt.setHint(R.string.mobile_hint);
            mobileEt.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        if(Utils.isBlank(password)){
            passwordEt.setHint(R.string.password_hint);
            passwordEt.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        beforeCall();

        HashMap<String, Object> map = new HashMap<>();
        map.put("countryCode", "65");
        map.put("mobile", mobile);
        map.put("password", password);
        map.put("type", "mobile");

        ApiServer.basePostRequestNoDisposable(Constants.SIGNUP, map, new TypeToken<ResponseData<LoginData>>() {
        }).subscribe(new NetworkSubscriber<LoginData>() {
            @Override
            protected void onSuccess(LoginData loginData) {
                loginData.mobile = mobile;
                loginData.countryCode = 65;
                if (listener != null){
                    listener.didRegistered(loginData);
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

    private void beforeCall(){
        registerBtn.setVisibility(View.INVISIBLE);
        loadingBar.start();
    }

    private void afterCall(){
        registerBtn.setVisibility(View.VISIBLE);
        loadingBar.stop();
    }

    public interface RegisterListener{
        void didRegistered(LoginData loginData);
    }
}
