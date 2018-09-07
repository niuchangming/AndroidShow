package ekoolab.com.show.fragments;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.activities.BirthdayActivity;
import ekoolab.com.show.activities.GenderActivity;
import ekoolab.com.show.activities.LoginActivity;
import ekoolab.com.show.activities.NameActivity;
import ekoolab.com.show.activities.NicknameActivity;
import ekoolab.com.show.activities.PersonActivity;
import ekoolab.com.show.activities.RegionActivity;
import ekoolab.com.show.activities.WhatsupActivity;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.utils.ViewHolder;

public class ProfileFragment extends BaseFragment implements View.OnClickListener{

    private BaseActivity activity;
    private RelativeLayout  name_rl,nickname_rl,gender_rl,birthday_rl,whatsup_rl,region_rl,header_rl;
    private TextView tv_name,tv_nickname,tv_gender,tv_birthday,tv_whatsup,tv_region;

    @Override
    public void onAttach(Context context) {
        activity = (BaseActivity) context;
        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile;
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onResultEvent(EventBusMsg eventBusMsg) {
        if (eventBusMsg.getFlag() == 0 || eventBusMsg.getFlag() == 1) {

        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        name_rl = holder.get(R.id.name_rl);
        name_rl.setOnClickListener(this);
        nickname_rl = holder.get(R.id.nickname_rl);
        nickname_rl.setOnClickListener(this);
        gender_rl = holder.get(R.id.gender_rl);
        gender_rl.setOnClickListener(this);
        birthday_rl = holder.get(R.id.birthday_rl);
        birthday_rl.setOnClickListener(this);
        whatsup_rl = holder.get(R.id.whatsup_rl);
        whatsup_rl.setOnClickListener(this);
        region_rl = holder.get(R.id.region_rl);
        region_rl.setOnClickListener(this);
        header_rl = holder.get(R.id.header_rl);
        header_rl.setOnClickListener(this);
        tv_name = holder.get(R.id.tv_name);
        tv_nickname = holder.get(R.id.tv_nickname);
        tv_gender = holder.get(R.id.tv_gender);
        tv_birthday = holder.get(R.id.tv_birthday);
        tv_whatsup = holder.get(R.id.tv_whatsup);
        tv_region = holder.get(R.id.tv_region);


    }


    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch(view.getId()){
            case R.id.header_rl:
                intent = new Intent(getContext(), PersonActivity.class);
                getContext().startActivity(intent);
                break;

            case R.id.name_rl:
                intent = new Intent(getContext(), NameActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.nickname_rl:
                intent = new Intent(getContext(), NicknameActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.gender_rl:
                intent = new Intent(getContext(), GenderActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.birthday_rl:
                intent = new Intent(getContext(), BirthdayActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.whatsup_rl:
                intent = new Intent(getContext(), WhatsupActivity.class);
                getContext().startActivity(intent);
                break;
            case R.id.region_rl:
                intent = new Intent(getContext(), RegionActivity.class);
                getContext().startActivity(intent);
                break;
        }
    }

    private void login(){
        Intent intent = new Intent(getContext(), LoginActivity.class);
        getContext().startActivity(intent);
    }

}
