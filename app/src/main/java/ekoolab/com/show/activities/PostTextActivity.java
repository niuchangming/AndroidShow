package ekoolab.com.show.activities;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.EventBusMsg;

public class PostTextActivity extends BaseActivity {

    private TextView tv_name,tv_cancel,tv_save,tv_permission;
    private EditText et_conment;
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
        et_conment = findViewById(R.id.et_comment);
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

            }
        });
    }
    @Override
    protected void initData() {
        super.initData();

    }
}
