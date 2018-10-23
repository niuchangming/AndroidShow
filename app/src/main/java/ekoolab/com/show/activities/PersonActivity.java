package ekoolab.com.show.activities;

import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.ProfileAdapter;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.fragments.submyvideos.MyCollectsFragment;
import ekoolab.com.show.fragments.submyvideos.MyVideoFragment;
import ekoolab.com.show.fragments.submyvideos.MymomentsFragment;

public class PersonActivity extends BaseActivity {

    private TextView tv_name,tv_cancel,tv_save;
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private ProfileAdapter pagerAdapter;
    private List<BaseFragment> fragments;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_person;
    }

    @Override
    protected void initViews() {
        super.initViews();
        indicatorTabLayout = findViewById(R.id.indicator_tab);
        viewPager = findViewById(R.id.viewpager);

        fragments = new ArrayList<>();
        fragments.add(new MyVideoFragment());
        fragments.add(new MyCollectsFragment());
        fragments.add(new MymomentsFragment());
        pagerAdapter =  new ProfileAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(pagerAdapter);
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


}
