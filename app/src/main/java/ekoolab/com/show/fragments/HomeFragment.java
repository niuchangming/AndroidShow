package ekoolab.com.show.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.activities.LoginActivity;
import ekoolab.com.show.adapters.HomeAdapter;
import ekoolab.com.show.fragments.subhomes.LiveFragment;
import ekoolab.com.show.fragments.subhomes.MomentFragment;
import ekoolab.com.show.fragments.subhomes.VideoFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.EventBusMsg;
import ekoolab.com.show.R;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.views.BorderShape;

import static ekoolab.com.show.utils.AuthUtils.AuthType.LOGGED;

public class HomeFragment extends BaseFragment implements View.OnClickListener{

    private BaseActivity activity;
    private RelativeLayout indicatorContainer;
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private HomeAdapter pagerAdapter;
    private Button liveBtn;
    private Button loginBtn;

    private List<BaseFragment> fragments;

    @Override
    public void onAttach(Context context) {
        activity = (BaseActivity) context;
        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
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

        if(AuthUtils.getInstance(getContext()).loginState() == LOGGED){
            loginBtn.setVisibility(View.GONE);
        }else{
            loginBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        liveBtn = holder.get(R.id.live_btn);
        liveBtn.setOnClickListener(this);
        loginBtn = holder.get(R.id.login_btn);
        loginBtn.setOnClickListener(this);

        fragments = new ArrayList<>();
        fragments.add(new MomentFragment());
        fragments.add(new VideoFragment());
        fragments.add(new LiveFragment());

        viewPager = holder.get(R.id.viewpager);
        pagerAdapter =  new HomeAdapter(getFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);

        ShapeDrawable lineDrawable = new ShapeDrawable(new BorderShape(new RectF(0, 0, 0, 1)));
        lineDrawable.getPaint().setColor(getResources().getColor(R.color.extraGray));
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{
                new ColorDrawable(getResources().getColor(R.color.colorWhite)),
                lineDrawable
        });

        indicatorContainer = holder.get(R.id.indicator_tab_container);
        indicatorContainer.setBackground(layerDrawable);

        indicatorTabLayout = holder.get(R.id.indicator_tab);
        indicatorTabLayout.setupWithViewPager(viewPager);
        indicatorTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                LinearLayout tabLayout = (LinearLayout) ((ViewGroup) indicatorTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tabLayout.getChildAt(1);
                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.BOLD);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                LinearLayout tabLayout = (LinearLayout)((ViewGroup) indicatorTabLayout.getChildAt(0)).getChildAt(tab.getPosition());
                TextView tabTextView = (TextView) tabLayout.getChildAt(1);
                tabTextView.setTypeface(tabTextView.getTypeface(), Typeface.NORMAL);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    @Override
    protected void initData() {
        super.initData();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.login_btn:
                login();
                break;
            case R.id.live_btn:
                break;
        }
    }

    private void login(){
        Intent intent = new Intent(getContext(), LoginActivity.class);
        getContext().startActivity(intent);
    }

}
