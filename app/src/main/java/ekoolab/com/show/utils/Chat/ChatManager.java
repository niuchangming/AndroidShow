package ekoolab.com.show.utils.Chat;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.orhanobut.logger.Logger;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ekoolab.com.show.beans.ChatMessage;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.beans.enums.MessageType;
import ekoolab.com.show.utils.AuthUtils;

public class ChatManager extends SendBird.ChannelHandler implements SendBird.ConnectionHandler{
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT";
    private static ChatManager chatManager;
    private Set<ChatManagerListener> chatManagerListenerSet;

    private Context context;
    private Map<String, GroupChannel> channelMap;

    public synchronized static ChatManager getInstance(Context context){
        if (chatManager == null) {
            chatManager = new ChatManager();
        }
        chatManager.context = context;
        return chatManager;
    }

    private ChatManager (){
        chatManagerListenerSet = new HashSet<>();
        channelMap = new HashMap<>();
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, this);
        SendBird.addConnectionHandler(CHANNEL_HANDLER_ID, this);
    }

    public void register(ChatManagerListener listener){
        chatManagerListenerSet.add(listener);
    }

    public void unregister(ChatManagerListener listener){
        if(chatManagerListenerSet.contains(listener)){
            chatManagerListenerSet.remove(listener);
        }
    }

    @Override
    public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {

        if (baseMessage instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) baseMessage;

            GroupChannel channel = channelMap.get(userMessage.getSender().getUserId());
            if(channel != null){
                ChatMessage chatMessage = ChatMessage.createByComing(context, userMessage);
                chatMessage.save(context);

                for (ChatManagerListener listener : chatManagerListenerSet) {
                    listener.didReceivedMessage(chatMessage);
                }
            }
        }
    }

    public void login(final SendBird.ConnectHandler handler) {
        String userId = "";
        if(AuthUtils.getInstance(context).loginState() == AuthUtils.AuthType.LOGGED){
            userId = AuthUtils.getInstance(context).getUserCode();
        }else{
            userId = AuthUtils.getInstance(context).getTempUserId();
        }

        SendBird.connect(userId, new SendBird.ConnectHandler() {
            @Override
            public void onConnected(User user, SendBirdException e) {
                if (handler != null) {
                    handler.onConnected(user, e);
                }
            }
        });
    }

    public void updateCurrentUserInfo(final String nickname, final String profileUrl) {
        SendBird.updateCurrentUserInfo(nickname, profileUrl, new SendBird.UserInfoUpdateHandler() {
            @Override
            public void onUpdated(SendBirdException e) {
                if (e != null) {
                    Logger.i("SBird Update User profile failed: " + e.getMessage());
                    return;
                }
            }
        });
    }

    public void createChannelWith(final Friend friend, final GroupChannel.GroupChannelCreateHandler handler){
        List<String> userIds = new ArrayList<>();
        userIds.add(friend.userCode);
        userIds.add(AuthUtils.getInstance(context).getUserCode());

        GroupChannel.createChannelWithUserIds(userIds, true, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if(handler != null){
                    handler.onResult(groupChannel, e);
                }
                if (e != null) {
                    return;
                }

                channelMap.put(friend.userCode, groupChannel);
            }
        });
    }

    public void loadChannelByFriend(final Friend friend, final GroupChannel.GroupChannelGetHandler handler){
        GroupChannel.getChannel(friend.channelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (handler != null) {
                    handler.onResult(groupChannel, e);
                }
                if(e != null) return;
                channelMap.put(friend.userCode, groupChannel);
            }
        });
    }

    public UserMessage sendMessage(String messageBody, String receiverId, MessageType messageType, BaseChannel.SendUserMessageHandler handler){

        GroupChannel groupChannel = channelMap.get(receiverId);
        if(groupChannel != null){
            List<String> targetLanguages = new ArrayList<>();
            targetLanguages.add("zh-CHS");

            UserMessage userMessage = groupChannel.sendUserMessage(messageBody, "", messageType.getName(), targetLanguages,
                    new BaseChannel.SendUserMessageHandler() {
                @Override
                public void onSent(UserMessage userMessage, SendBirdException e) {
                    if(handler != null){
                        handler.onSent(userMessage, e);
                    }
                }
            });

            return userMessage;
        }

        return null;

    }

    public void logout(final SendBird.DisconnectHandler handler) {
        SendBird.disconnect(new SendBird.DisconnectHandler() {
            @Override
            public void onDisconnected() {
                if (handler != null) {
                    handler.onDisconnected();
                }
            }
        });
    }

    @Override
    public void onReconnectStarted() {
        Logger.i("Reconnection started");
    }

    @Override
    public void onReconnectSucceeded() {
        Logger.i("Reconnection succeeded");
    }

    @Override
    public void onReconnectFailed() {
        Logger.i("Reconnection failed");
    }

    public interface ChatManagerListener {
        void didReceivedMessage(ChatMessage chatMessage);
    }

}






























