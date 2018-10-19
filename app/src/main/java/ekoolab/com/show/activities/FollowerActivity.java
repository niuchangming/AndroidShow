package ekoolab.com.show.activities;



import org.greenrobot.eventbus.EventBus;

import ekoolab.com.show.R;

public class FollowerActivity extends BaseActivity {
    @Override
    protected int getLayoutId(){
        return R.layout.activity_followers;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    protected void initViews() {
        super.initViews();


    }

}
