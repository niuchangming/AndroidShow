package ekoolab.com.show.activities;

import android.widget.TextView;

import ekoolab.com.show.R;

public class NameActivity extends BaseActivity {

    private TextView tv_name,tv_cancel,tv_save;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_name;
    }

    @Override
    protected void initViews() {
        super.initViews();
        tv_name = findViewById(R.id.tv_name);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_save = findViewById(R.id.tv_save);
        tv_name.setText(getResources().getString(R.string.name));
    }
    @Override
    protected void initData() {
        super.initData();

    }

}
