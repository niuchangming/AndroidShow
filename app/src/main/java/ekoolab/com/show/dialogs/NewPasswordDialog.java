package ekoolab.com.show.dialogs;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.rey.material.app.SimpleDialog;
import com.rey.material.widget.ProgressView;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class NewPasswordDialog extends SimpleDialog implements View.OnClickListener {


    private final String TAG = "RegisterDialog";;

    private EditText new_password_et;
    private EditText confirm_password_et;
    private ProgressView loadingBar;
    private Button cancel_btn;
    private Button save_btn;
    private NewPasswordDialog.ChangePasswordListener listener;
    private LoginData loginData;

    public NewPasswordDialog(Context context) {
        this(context, R.style.SimpleDialogLight);
    }


    public NewPasswordDialog(Context context, int style) {
        super(context, style);
        contentView(R.layout.dialog_new_password);

        if(NewPasswordDialog.ChangePasswordListener.class.isAssignableFrom(context.getClass())){
            listener = (NewPasswordDialog.ChangePasswordListener)context;
        }

        initViews();
    }

    public void setLoginData(LoginData loginData) {
        this.loginData = loginData;
    }

    private void initViews(){
        loadingBar = findViewById(R.id.save_pv);
        new_password_et = findViewById(R.id.new_password_et);
        confirm_password_et = findViewById(R.id.confirm_password_et);

        cancel_btn = findViewById(R.id.cancel_btn);
        cancel_btn.setOnClickListener(this);

        save_btn = findViewById(R.id.save_btn);
        save_btn.setOnClickListener(this);

//        TextView.OnEditorActionListener enterListener = new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if(actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_DOWN){
//                    save();
//                }
//                return true;
//            }
//        };
//
//        new_password_et.setOnEditorActionListener(enterListener);
//        confirm_password_et.setOnEditorActionListener(enterListener);

    }

    @Override
    public void onClick(View view) {

        switch(view.getId()){
            case R.id.cancel_btn:
                cancel();
                break;
            case R.id.save_btn:
                save();
                break;
        }

    }

    private void save(){
        String password_1 = new_password_et.getText().toString().trim();
        String password_2 = confirm_password_et.getText().toString().trim();
        String mobile = this.loginData.mobile;
        String token = this.loginData.token;
        System.out.println("In newPasswordDialog see mobile: " + mobile);
        System.out.println("before check password. 1: " + password_1 + ". 2: "+ password_2);
        if (Utils.isBlank(password_1)) {
            new_password_et.setHint(R.string.new_password);
            new_password_et.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        if (Utils.isBlank(password_2)) {
            confirm_password_et.setHint(R.string.confirm_new_password);
            confirm_password_et.setHintTextColor(ContextCompat.getColor(getContext(), R.color.colorRed));
            return;
        }

        if(!password_1.equals(password_2)){
            ToastUtils.showToast("Please enter the same password.");
            return;
        }

        beforeSave();
        HashMap<String, Object> map = new HashMap<>(3);
        map.put("pwd1", password_1);
        map.put("pwd2", password_2);
        map.put("token", token);

        ApiServer.basePostRequestNoDisposable(Constants.SAVE_PASSWORD, map, new TypeToken<ResponseData<LoginData>>() {
        }).subscribe(new NetworkSubscriber<LoginData>() {
            @Override
            protected void onSuccess(LoginData loginData) {
                System.out.println("Save successful: ");
                if (listener != null){
                    System.out.println("listener not null");
                    loginData.mobile = mobile;
                    loginData.token = token;
                    listener.didChangePassword(loginData);
                }
                confirm_password_et.clearFocus();
                InputMethodManager imm = (InputMethodManager) confirm_password_et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                System.out.println("KeyBoardUtil------InputMethodManager="+imm.toString()+"view.getWindowToken()="+confirm_password_et.getWindowToken().toString());
                imm.hideSoftInputFromWindow(confirm_password_et.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                afterSave();
                cancel();
            }

            @Override
            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                System.out.println("Save failed");
                afterSave();
                return super.dealHttpException(code, errorMsg, e);
            }

        });

    }

    private void beforeSave(){
        save_btn.setVisibility(View.INVISIBLE);
        loadingBar.start();
    }

    private void afterSave(){
        save_btn.setVisibility(View.VISIBLE);
        loadingBar.stop();
    }

    public interface ChangePasswordListener{
        void didChangePassword(LoginData loginData);
    }

}
