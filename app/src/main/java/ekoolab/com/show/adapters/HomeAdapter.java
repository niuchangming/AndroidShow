package ekoolab.com.show.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.fragments.BaseFragment;

public class HomeAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> fragments;
    private int[] subTitleIds = {R.string.moment, R.string.video, R.string.live};
    private Context context;


    public HomeAdapter(Context context, FragmentManager fm, List<BaseFragment> fragments) {
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
        return context.getString(subTitleIds[position]);
    }
}
