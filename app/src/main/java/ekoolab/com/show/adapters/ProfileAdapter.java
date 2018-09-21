package ekoolab.com.show.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.Constants;

public class ProfileAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> fragments;


    public ProfileAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return Constants.profileIndicatorTitles[position];
    }
}
