package ekoolab.com.show.fragments;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.adapters.HomeAdapter;
import ekoolab.com.show.fragments.subhomes.LiveFragment;
import ekoolab.com.show.fragments.subhomes.MomentFragment;
import ekoolab.com.show.fragments.subhomes.VideoFragment;
import ekoolab.com.show.R;
import ekoolab.com.show.utils.ViewHolder;

public class HomeFragment extends BaseFragment{
    private TabLayout indicatorTabLayout;
    private ViewPager viewPager;
    private HomeAdapter pagerAdapter;

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

    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
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

        createTabIcons();

    }

    private void createTabIcons() {
        LinearLayout tab1Layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.tab_titleview, null);
        TextView title1Tv = tab1Layout.findViewById(R.id.title_tv);
        TextView subTitle1Tv = tab1Layout.findViewById(R.id.sub_title_tv);
        title1Tv.setText("社交");
        subTitle1Tv.setText(getString(R.string.moment));
        indicatorTabLayout.getTabAt(0).setCustomView(tab1Layout);

        LinearLayout tab2Layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.tab_titleview, null);
        TextView title2Tv = tab2Layout.findViewById(R.id.title_tv);
        TextView subTitle2Tv = tab2Layout.findViewById(R.id.sub_title_tv);
        title2Tv.setText("秀");
        subTitle2Tv.setText(getString(R.string.video));
        indicatorTabLayout.getTabAt(1).setCustomView(tab2Layout);

        LinearLayout tab3Layout = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.tab_titleview, null);
        TextView title3Tv = tab3Layout.findViewById(R.id.title_tv);
        TextView subTitle3Tv = tab3Layout.findViewById(R.id.sub_title_tv);
        title3Tv.setText("天秀");
        subTitle3Tv.setText(getString(R.string.live));
        indicatorTabLayout.getTabAt(2).setCustomView(tab3Layout);
    }

    @Override
    protected void initData() {
        super.initData();
    }


    public int getCurrentPagerIndex(){
        return viewPager.getCurrentItem();
    }

}
