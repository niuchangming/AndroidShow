package ekoolab.com.show.activities;

import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;

public class HobbyActivity extends BaseActivity implements View.OnClickListener {

    private TextView tv_hobby, tv_cancel,tv_save;
    private String hobby;
    private EditText et_hobby;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_hobby;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_hobby = findViewById(R.id.tv_name);
        tv_hobby.setText(getResources().getString(R.string.name));
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_cancel.setOnClickListener(this);
        tv_save = findViewById(R.id.tv_save);
        tv_save.setOnClickListener(this);
        et_hobby = findViewById(R.id.et_name);
    }
    @Override
    protected void initData() {
        super.initData();

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View view){
        switch (view.getId()){
            case R.id.tv_cancel:
                onBackPressed();
                break;
            case R.id.tv_save:
//                setName();
                break;
        }
    }

    private void setName(){
        hobby = et_hobby.getText().toString();
        Utils.hideInput(et_hobby);
        tv_save.setVisibility(View.INVISIBLE);
        setViewClickable(false);
        HashMap<String, String> map = new HashMap<>(2);
        map.put("hobby", hobby);
        map.put("token", AuthUtils.getInstance(HobbyActivity.this).getApiToken());
        ApiServer.basePostRequest(this, Constants.UPDATE_USERPROFILE, map,
                new TypeToken<ResponseData<TextPicture>>() {
                })
                .subscribe(new NetworkSubscriber<TextPicture>() {
                    @Override
                    protected void onSuccess(TextPicture textPicture) {
                        ToastUtils.showToast("Saved");
                        Intent intent = new Intent();
                        intent.putExtra("name", hobby);
                        setResult(RESULT_OK, intent);
                        finish();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        System.out.println("===errorMsg==="+errorMsg);
                        tv_save.setVisibility(View.VISIBLE);
                        setViewClickable(true);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void setViewClickable(boolean clickable) {
        tv_cancel.setClickable(clickable);
        tv_save.setClickable(clickable);
        et_hobby.setClickable(clickable);
        et_hobby.setEnabled(clickable);
    }
}
