package ekoolab.com.show.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.fragments.BaseFragment;
import ekoolab.com.show.utils.Constants;

public class ProfileAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> fragments;
    private int[] titleIds = {R.string.my_video, R.string.my_favorite, R.string.my_moment};
    private Context context;


    public ProfileAdapter(Context context, FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.fragments = fragments;
        this.context = context;
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
        return context.getString(titleIds[position]);
    }
}
