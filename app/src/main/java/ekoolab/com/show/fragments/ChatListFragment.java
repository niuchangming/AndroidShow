package ekoolab.com.show.fragments;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;
import com.santalu.emptyview.EmptyView;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.ChatActivity;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.utils.Chat.ChatManager;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.utils.ViewHolder;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class ChatListFragment extends BaseFragment implements BaseQuickAdapter.OnItemClickListener{
    private EmptyView emptyView;
    private RecyclerView recyclerView;
    private BaseQuickAdapter<Friend, BaseViewHolder> adapter;
    private List<Friend> friends = new ArrayList<>();;
    private boolean isFirstLoad;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_chat_list;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        isFirstLoad = true;
    }

    @Override
    protected void initViews(ViewHolder holder, View root) {
        emptyView = holder.get(R.id.empty_view);
        recyclerView=  holder.get(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.addItemDecoration(new LinearItemDecoration(mContext,
                1, R.color.colorLightGray, 0));

        bindAdapter(recyclerView);
    }

    @Override
    protected void initData() {
        super.initData();
        if (isFirstLoad || friends.size() == 0){
            isFirstLoad = false;
            friends.addAll(getFriendsFromLocal());
            if (friends.size() == 0) {
                emptyView.showEmpty();
            }else{
                adapter.notifyDataSetChanged();
                prepareForConnectChannel(friends);
            }
        }
    }

    private void bindAdapter(RecyclerView recyclerView) {
        this.adapter = new BaseQuickAdapter<Friend, BaseViewHolder>(R.layout.item_chat_list, friends) {
            @Override
            protected void convert(BaseViewHolder helper, Friend item) {
                ImageLoader.displayImageAsCircle(item.avatar.small, helper.getView(R.id.avatar_iv));
                helper.setText(R.id.name_tv, Utils.getDisplayName(item.name, item.nickName));
            }
        };

        this.adapter.setHasStableIds(false);
        this.adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Friend friend = friends.get(position);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.FRIEND_DATA, friend);
        startActivity(intent);
    }

    public void friendSyncCompleted(){
        List<Friend> newFriends = getFriendsFromLocal();
        if(newFriends.size() > 0){
            friends.clear();
            friends.addAll(newFriends);

            emptyView.showContent();
            recyclerView.getAdapter().notifyDataSetChanged();
            prepareForConnectChannel(newFriends);
        }
    }


    private void prepareForConnectChannel(final List<Friend> friends){
        if (SendBird.getCurrentUser() == null) {
            ChatManager.getInstance(getContext()).login(new SendBird.ConnectHandler() {
                @Override
                public void onConnected(User user, SendBirdException e) {
                    if ( e != null) return;
                    connectChannelForFriends(friends);
                }
            });
        }else{
            connectChannelForFriends(friends);
        }
    }
    private void connectChannelForFriends(List<Friend> friends){
        for (Friend friend : friends) {
            if (Utils.isBlank(friend.channelUrl)) {
                ChatManager.getInstance(getContext()).createChannelWith(friend, new GroupChannel.GroupChannelCreateHandler() {
                    @Override
                    public void onResult(GroupChannel groupChannel, SendBirdException e) {
                        if (e != null) return;
                        friend.channelUrl = groupChannel.getUrl();
                        friend.save(getContext());
                    }
                });
            } else {
                ChatManager.getInstance(getContext()).loadChannelByFriend(friend, new GroupChannel.GroupChannelGetHandler() {
                    @Override
                    public void onResult(GroupChannel groupChannel, SendBirdException e) {

                    }
                });
            }
        }
    }

    public List<Friend> getFriendsFromLocal(){
        List<Friend> allFriends = Friend.getAllFriends(getActivity(), Constants.FriendTableColumns.userId + " IS NOT NULL AND "
                + Constants.FriendTableColumns.isAppUser + " =? ", new String[]{"1"});
        return allFriends;
    }
}
