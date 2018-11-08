package ekoolab.com.show.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.activities.AnchorRegisterActivity;
import ekoolab.com.show.activities.BroadcastActivity;
import ekoolab.com.show.activities.LoginActivity;
import ekoolab.com.show.adapters.HomeAdapter;
import ekoolab.com.show.fragments.subhomes.LiveFragment;
import ekoolab.com.show.fragments.subhomes.MomentFragment;
import ekoolab.com.show.fragments.subhomes.VideoFragment;
import ekoolab.com.show.fragments.subhomes.WebFragment;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.R;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;

import static ekoolab.com.show.activities.BaseActivity.IS_FULL_SCREEN;
import static ekoolab.com.show.utils.AuthUtils.AuthType.LOGGED;

public class HomeFragment extends BaseFragment implements View.OnClickListener{
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private HomeAdapter pagerAdapter;
    private Button liveBtn;
    private Button loginBtn;

    private List<BaseFragment> fragments;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }


    @Override
    public void onStart() {
        super.onStart();

        if(AuthUtils.getInstance(getContext()).loginState() == LOGGED){
            loginBtn.setVisibility(View.GONE);
        }else{
            loginBtn.setVisibility(View.VISIBLE);
        }
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
        pagerAdapter =  new HomeAdapter(getContext(), getFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(pagerAdapter);

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
                if (AuthUtils.getInstance(getActivity()).loginState() != LOGGED
                        || Utils.isBlank(AuthUtils.getInstance(getActivity()).getApiToken())){
                    login();
                    return;
                }

                if (AuthUtils.getInstance(getActivity()).getRole() != 2) {
                    Intent registerAnchorIntent = new Intent(getActivity(), AnchorRegisterActivity.class);
                    startActivity(registerAnchorIntent);
                    return;
                }

                Intent broadcastIntent = new Intent(getActivity(), BroadcastActivity.class);
                broadcastIntent.putExtra(IS_FULL_SCREEN, true);
                startActivity(broadcastIntent);
                break;
        }
    }

    private void login(){
        Intent intent = new Intent(getContext(), LoginActivity.class);
        getContext().startActivity(intent);
    }

}
