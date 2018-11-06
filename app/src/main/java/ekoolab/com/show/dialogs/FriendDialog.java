package ekoolab.com.show.dialogs;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;
import com.santalu.emptyview.EmptyView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class FriendDialog extends XXDialog implements BaseQuickAdapter.OnItemClickListener{
    private EmptyView emptyView;
    private RecyclerView recyclerView;
    private BaseQuickAdapter<Friend, BaseViewHolder> adapter;
    private List<Friend> friends;
    private boolean isFirstLoad;
    private Map<String, Friend> selectedFriendMap;
    private FriendDialogListener listener;

    public FriendDialog(Context context, int layoutId) {
        this(context, layoutId, false);
    }

    public FriendDialog(Context context, int layoutId, boolean isBgTransparent) {
        super(context, layoutId, isBgTransparent);

        if(FriendDialogListener.class.isAssignableFrom(context.getClass())){
            listener = (FriendDialogListener)context;
        }
    }

    @Override
    public void convert(DialogViewHolder holder) {
        emptyView = holder.getView(R.id.empty_view);
        recyclerView=  holder.getView(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new LinearItemDecoration(getContext(), 1, R.color.colorDarkBlue, 0));
        recyclerView.getLayoutParams().height = DisplayUtils.getScreenHeight() - DisplayUtils.getNavBarHeight() - DisplayUtils.getStatusBarHeight();

        initData();
    }

    private void bindAdapter(RecyclerView recyclerView) {
        this.adapter = new BaseQuickAdapter<Friend, BaseViewHolder>(R.layout.item_friend_picker, friends) {
            @Override
            protected void convert(BaseViewHolder helper, Friend item) {
                ImageLoader.displayImageAsCircle(item.avatar.small, helper.getView(R.id.avatar_iv));
                helper.setText(R.id.name_tv, Utils.getDisplayName(item.name, item.nickName));

                ImageView tickIv = helper.getView(R.id.tick_iv);
                if(getSelectedFriendMap().containsKey(item.userCode)){
                    tickIv.setVisibility(View.VISIBLE);
                }else{
                    tickIv.setVisibility(View.GONE);
                }

            }
        };

        this.adapter.setHasStableIds(false);
        this.adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void initData(){
        if (friends == null) {
            friends = new ArrayList<>();
        }

        if (isFirstLoad || friends.size() == 0){
            isFirstLoad = false;
            friends.addAll(getFriendsFromLocal());
            if (friends.size() == 0) {
                emptyView.showEmpty();
            }else{
                emptyView.showContent();
            }
        }

        bindAdapter(recyclerView);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Friend friend = friends.get(position);
        if(getSelectedFriendMap().containsKey(friend.userCode)){
            getSelectedFriendMap().remove(friend.userCode);
        }else{
            getSelectedFriendMap().put(friend.userCode, friend);
        }
        recyclerView.getAdapter().notifyItemChanged(position);

        if(listener != null){
            listener.friendChanged(friend);
        }
    }

    public List<Friend> getFriendsFromLocal(){
        List<Friend> allFriends = Friend.getAllFriends(getContext(), Constants.FriendTableColumns.userId + " IS NOT NULL AND "
                + Constants.FriendTableColumns.isAppUser + " =? ", new String[]{"1"});
        return allFriends;
    }

    public Map<String, Friend> getSelectedFriendMap() {
        if(selectedFriendMap == null){
            selectedFriendMap = new HashMap<>();
        }
        return selectedFriendMap;
    }

    public void setSelectedFriendMap(Map<String, Friend> selectedFriendMap) {
        this.selectedFriendMap = selectedFriendMap;
        adapter.notifyDataSetChanged();
    }

    public interface FriendDialogListener{
        void friendChanged(Friend friend);
    }
}
