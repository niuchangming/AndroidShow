package ekoolab.com.show.fragments;

import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;

import ekoolab.com.show.views.BorderShape;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.R;
import ekoolab.com.show.views.TabButton;
import ekoolab.com.show.utils.ViewHolder;

public class TabFragment extends BaseFragment implements View.OnClickListener {
    private TabButton tabHome;
    private TabButton tabZSC;
    private TabButton tabEmart;
    private TabButton tabProfile;
    private ImageButton cameraBtn;

    private int mContainerId;
    private TabButton mCurrentNavButton;
    private OnTabBarSelectedListener mOnNavigationReselectListener;

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_tabbar;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        tabHome = holder.get(R.id.tab_item_home);
        tabZSC = holder.get(R.id.tab_item_zsc);
        tabEmart = holder.get(R.id.tab_item_emart);
        tabProfile = holder.get(R.id.tab_item_profile);
        cameraBtn = holder.get(R.id.tab_item_camera);

        holder.setOnClickListener(this, R.id.tab_item_home);
        holder.setOnClickListener(this, R.id.tab_item_zsc);
        holder.setOnClickListener(this, R.id.tab_item_emart);
        holder.setOnClickListener(this, R.id.tab_item_profile);
        holder.setOnClickListener(this, R.id.tab_item_camera);

        ShapeDrawable lineDrawable = new ShapeDrawable(new BorderShape(new RectF(0, 1, 0, 0)));
        lineDrawable.getPaint().setColor(getResources().getColor(R.color.extraGray));
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{
                new ColorDrawable(getResources().getColor(R.color.colorWhite)),
                lineDrawable
        });
        root.setBackground(layerDrawable);

        if (Constants.tabBarTitles.length < 4) {
            return;
        }
        tabHome.init(R.drawable.tab_icon_home, Constants.tabBarTitles[0], HomeFragment.class);

        tabZSC.init(R.drawable.tab_icon_zsc, Constants.tabBarTitles[1], HomeFragment.class);

        tabEmart.init(R.drawable.tab_icon_emart, Constants.tabBarTitles[2], HomeFragment.class);

        tabProfile.init(R.drawable.tab_icon_profile, Constants.tabBarTitles[3], ProfileFragment.class);

        clearOldFragment();
        doSelect(tabHome);
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TabButton) {
            TabButton nav = (TabButton) v;
            doSelect(nav);
        } else if (v.getId() == R.id.tab_item_camera) {
            mOnNavigationReselectListener.onCenterCameraClick();
        }

    }

    public void setup(int contentId, OnTabBarSelectedListener listener) {
        mContainerId = contentId;
        mOnNavigationReselectListener = listener;
    }

    public void select(int index) {
        if (tabProfile != null)
            doSelect(tabProfile);
    }

    private void clearOldFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();
        if (transaction == null || fragments == null || fragments.size() == 0)
            return;
        boolean doCommit = false;
        for (Fragment fragment : fragments) {
            if (fragment != this && fragment != null && fragment instanceof BaseFragment) {
                transaction.remove(fragment);
                doCommit = true;
            }
        }
        if (doCommit) {
            transaction.commitNow();
        }
    }

    private void doSelect(TabButton newNavButton) {
        TabButton oldNavButton = null;
        if (mCurrentNavButton != null) {
            oldNavButton = mCurrentNavButton;
            if (oldNavButton == newNavButton) {
                onReselect(oldNavButton);
                return;
            }
            oldNavButton.setSelected(false);
        }
        newNavButton.setSelected(true);
        doTabChanged(oldNavButton, newNavButton);
        mCurrentNavButton = newNavButton;
    }

    private void doTabChanged(TabButton oldNavButton, TabButton newNavButton) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        if (oldNavButton != null) {
            if (oldNavButton.getFragment() != null) {
                ft.detach(oldNavButton.getFragment());
            }
        }
        if (newNavButton != null) {
            if (newNavButton.getFragment() == null) {
                Fragment fragment = Fragment.instantiate(mContext,
                        newNavButton.getClx().getName(), null);
                ft.add(mContainerId, fragment, newNavButton.getTag());
                newNavButton.setFragment(fragment);
            } else {
                ft.attach(newNavButton.getFragment());
            }
        }
        ft.commit();
    }

    private void onReselect(TabButton navigationButton) {
        OnTabBarSelectedListener listener = mOnNavigationReselectListener;
        if (listener != null) {
            listener.onReselect(navigationButton);
        }
    }

    public interface OnTabBarSelectedListener {
        void onReselect(TabButton navigationButton);

        void onCenterCameraClick();
    }
}
