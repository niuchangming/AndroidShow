package ekoolab.com.show.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.UserMessage;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.beans.ChatMessage;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.TimeUtils;

import static ekoolab.com.show.beans.enums.SendState.*;

public class ChatMessageAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;

    private Activity activity;
    private List<ChatMessage> chatMessages;

    public ChatMessageAdapter(Activity activity, List<ChatMessage> chatMessages) {
        this.activity = activity;
        this.chatMessages = chatMessages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                View meView = activity.getLayoutInflater().inflate(R.layout.item_outgoing_text_message, parent, false);
                return new MeTextMessageHolder(meView);
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                View otherView = activity.getLayoutInflater().inflate(R.layout.item_incoming_text_message, parent, false);
                return new OtherTextMessageHolder(otherView);
            default:
                return null;

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        boolean isNewDay = false;
        ChatMessage chatMessage = chatMessages.get(position);

        if (position > 0) {
            ChatMessage prevMessage = chatMessages.get(position - 1);

            if (!TimeUtils.isSameDate(chatMessage.createAt, prevMessage.createAt)) {
                isNewDay = true;
            }
        } else{
            isNewDay = true;
        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                ((MeTextMessageHolder) holder).bind(chatMessage, isNewDay);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                ((OtherTextMessageHolder) holder).bind(chatMessage, isNewDay);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {

        ChatMessage chatMessage = chatMessages.get(position);
        if(chatMessage.senderId == AuthUtils.getInstance(activity).getUserCode()){
            return VIEW_TYPE_USER_MESSAGE_ME;
        } else {
            return VIEW_TYPE_USER_MESSAGE_OTHER;
        }

    }

    class MeTextMessageHolder extends RecyclerView.ViewHolder {
        private ImageView readStateIv;
        private TextView messageTv;
        private TextView dateTv;
        private TextView timeTv;

        public MeTextMessageHolder(View itemView) {
            super(itemView);

            readStateIv = itemView.findViewById(R.id.receipt_state_iv);
            messageTv = itemView.findViewById(R.id.chat_message_tv);
            dateTv = itemView.findViewById(R.id.chat_date_tv);
            timeTv = itemView.findViewById(R.id.chat_time_tv);
        }

        public void bind(final ChatMessage chatMessage, final boolean isNewDay) {
            messageTv.setText(chatMessage.message);

            if (chatMessage.sendState == SENT) {
                readStateIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_sent));
                readStateIv.setVisibility(View.VISIBLE);
            } else if (chatMessage.sendState == REACHED){
                readStateIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_reached));
                readStateIv.setVisibility(View.VISIBLE);
            } else if (chatMessage.sendState == READ){
                readStateIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_read));
                readStateIv.setVisibility(View.VISIBLE);
            } else {
                readStateIv.setVisibility(View.GONE);
            }

            if (isNewDay){
                dateTv.setVisibility(View.VISIBLE);
                dateTv.setText(TimeUtils.getDateStringByTimeStamp(chatMessage.createAt));
            }else{
                dateTv.setVisibility(View.GONE);
            }
            timeTv.setText(TimeUtils.getTimeByTimestamp(chatMessage.createAt));

        }
    }

    class OtherTextMessageHolder extends RecyclerView.ViewHolder {
        private ImageView avatarIv;
        private TextView messageTv;
        private TextView nameTv;
        private TextView dateTv;
        private TextView timeTv;

        public OtherTextMessageHolder(View itemView) {
            super(itemView);

            avatarIv = itemView.findViewById(R.id.avatar_iv);
            messageTv = itemView.findViewById(R.id.chat_message_tv);
            nameTv = itemView.findViewById(R.id.name_tv);
            dateTv = itemView.findViewById(R.id.chat_date_tv);
            timeTv = itemView.findViewById(R.id.chat_time_tv);
        }

        public void bind(final ChatMessage chatMessage, final boolean isNewDay) {
            ImageLoader.displayImageAsCircle(chatMessage.senderProfileUrl, avatarIv);
            messageTv.setText(chatMessage.message);
            nameTv.setText(chatMessage.senderName);
            timeTv.setText(TimeUtils.getTimeByTimestamp(chatMessage.createAt));

            if (isNewDay){
                dateTv.setVisibility(View.VISIBLE);
                dateTv.setText(TimeUtils.getDateStringByTimeStamp(chatMessage.createAt));
            }else{
                dateTv.setVisibility(View.GONE);
            }


        }
    }
}















