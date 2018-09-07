package ekoolab.com.show.dialogs;

import android.content.Context;
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

        beforeCall();
        AndroidNetworking.post(Constants.SIGNUP)
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
                            if (errorCode == 1) {;
                                AuthInfo authInfo = new AuthInfo(response);
                                authInfo.mobile = mobile;
                                authInfo.dialNo = "65";

                                if(listener != null) {
                                    listener.didRegistered(authInfo);
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

    private void beforeCall(){
        registerBtn.setVisibility(View.INVISIBLE);
        loadingBar.start();
    }

    private void afterCall(){
        registerBtn.setVisibility(View.VISIBLE);
        loadingBar.stop();
    }

    public interface RegisterListener{
        void didRegistered(AuthInfo authInfo);
    }
}
