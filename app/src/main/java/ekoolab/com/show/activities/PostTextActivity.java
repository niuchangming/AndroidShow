package ekoolab.com.show.activities;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.TextPicture;
import ekoolab.com.show.beans.Video;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ListUtils;

public class PostTextActivity extends BaseActivity {

    private TextView tv_name,tv_cancel,tv_save,tv_permission;
    private EditText et_content;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_post_text;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_save = findViewById(R.id.tv_save);
        tv_permission = findViewById(R.id.tv_permission);
        et_content = findViewById(R.id.et_content);
        tv_name.setText(getResources().getString(R.string.moment));
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PostTextActivity.this.finish();
            }
        });
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postText();
            }
        });
    }

    private void postText() {
        HashMap<String,String> map = new HashMap<>(4);
        map.put("body", et_content.getText().toString());
        map.put("type", "text");
        map.put("permission", "private");
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        ApiServer.basePostRequest(this, Constants.TextPost, map,
                new TypeToken<ResponseData<TextPicture>>() {
                })
                .subscribe(new NetworkSubscriber<TextPicture>() {
                    @Override
                    protected void onSuccess(TextPicture textPicture) {
                        try {
                            System.out.println("===body==="+textPicture.body);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    @Override
    protected void initData() {
        super.initData();

    }
}
