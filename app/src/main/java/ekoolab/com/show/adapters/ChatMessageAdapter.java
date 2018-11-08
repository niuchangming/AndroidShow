package ekoolab.com.show.adapters;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.DownloadListener;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;

import java.io.File;
import java.sql.Time;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.ChatMessage;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.beans.ResourceFile;
import ekoolab.com.show.beans.enums.MessageType;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Chat.ChatMessageItemLayout;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.ImageLoader;
import ekoolab.com.show.utils.TimeUtils;
import ekoolab.com.show.utils.ToastUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.AudioView.AudioPlayManager;
import ekoolab.com.show.views.AudioView.IAudioPlayListener;

import static ekoolab.com.show.beans.enums.SendState.*;

public class ChatMessageAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;
    private static final int VIEW_TYPE_AUDIO_ME = 12;
    private static final int VIEW_TYPE_AUDIO_OTHER = 13;

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
            case VIEW_TYPE_AUDIO_ME:
                View meAudioView = activity.getLayoutInflater().inflate(R.layout.item_outgoing_audio_message, parent, false);
                return new MeAudioMessageHolder(meAudioView);
            case VIEW_TYPE_AUDIO_OTHER:
                View otherAudioView = activity.getLayoutInflater().inflate(R.layout.item_incoming_audio_message, parent, false);
                return new OtherAudioMessageHolder(otherAudioView);
            default:
                return null;

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        boolean isNewDay = false;
        boolean isContinuous = false;
        ChatMessage chatMessage = chatMessages.get(position);

        if (position < chatMessages.size() - 1) {
            ChatMessage prevMessage = chatMessages.get(position + 1);
            if (!TimeUtils.isSameDate(chatMessage.createAt, prevMessage.createAt)) {
                isNewDay = true;
            }

            if(Utils.equals(chatMessage.senderId, prevMessage.senderId) || isNewDay){
                isContinuous = true;
            }
        } else if (position == chatMessages.size() - 1) {
            isNewDay = true;
        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                ((MeTextMessageHolder) holder).bind(chatMessage, isNewDay);
                break;
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                ((OtherTextMessageHolder) holder).bind(chatMessage, isNewDay, isContinuous);
                break;
            case VIEW_TYPE_AUDIO_ME:
                ((MeAudioMessageHolder) holder).bind(chatMessage, isNewDay);
                break;
            case VIEW_TYPE_AUDIO_OTHER:
                ((OtherAudioMessageHolder) holder).bind(chatMessage, isNewDay, isContinuous);
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
        if(Utils.equals(AuthUtils.getInstance(activity).getUserCode(), chatMessage.senderId)){
            if(chatMessage.messageType == MessageType.AUDIO){
                return VIEW_TYPE_AUDIO_ME;
            }else {
                return VIEW_TYPE_USER_MESSAGE_ME;
            }
        } else {
            if(chatMessage.messageType == MessageType.AUDIO){
                return VIEW_TYPE_AUDIO_OTHER;
            }else{
                return VIEW_TYPE_USER_MESSAGE_OTHER;
            }
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

            if (chatMessage.sendState == SENDING) {
                readStateIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_sending));
                readStateIv.setVisibility(View.VISIBLE);
            } else if (chatMessage.sendState == SENT){
                readStateIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_sent));
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

        public void bind(final ChatMessage chatMessage, final boolean isNewDay, final boolean isContinuous) {
            ImageLoader.displayImageAsCircle(chatMessage.senderProfileUrl, avatarIv);
            messageTv.setText(chatMessage.message);
            if (isContinuous) {
                nameTv.setVisibility(View.GONE);
            }else{
                nameTv.setVisibility(View.VISIBLE);
                nameTv.setText(chatMessage.senderName);
            }
            timeTv.setText(TimeUtils.getTimeByTimestamp(chatMessage.createAt));

            if (isNewDay){
                dateTv.setVisibility(View.VISIBLE);
                dateTv.setText(TimeUtils.getDateStringByTimeStamp(chatMessage.createAt));
            }else{
                dateTv.setVisibility(View.GONE);
            }
        }
    }

    class MeAudioMessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ChatMessageItemLayout rootView;
        private CardView cardMessageCard;
        private ImageView receiptIv;
        private TextView durationTv;
        private TextView dateTv;
        private TextView timeTv;

        public MeAudioMessageHolder(View itemView) {
            super(itemView);
            rootView = (ChatMessageItemLayout) itemView;
            cardMessageCard = itemView.findViewById(R.id.chat_message_card);
            receiptIv = itemView.findViewById(R.id.receipt_state_iv);
            durationTv = itemView.findViewById(R.id.duration_tv);
            dateTv = itemView.findViewById(R.id.chat_date_tv);
            timeTv = itemView.findViewById(R.id.chat_time_tv);
        }

        public void bind(final ChatMessage chatMessage, final boolean isNewDay) {
            rootView.setChatMessage(chatMessage);
            timeTv.setText(TimeUtils.getTimeByTimestamp(chatMessage.createAt));

            long milliseconds = chatMessage.resourceFile.duration;
            long seconds = (long) ((milliseconds / 1000) % 60);
            durationTv.setText(TimeUtils.secondsToString((int)seconds));

            if (isNewDay){
                dateTv.setVisibility(View.VISIBLE);
                dateTv.setText(TimeUtils.getDateStringByTimeStamp(chatMessage.createAt));
            }else{
                dateTv.setVisibility(View.GONE);
            }

            if (chatMessage.sendState == SENDING) {
                receiptIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_sending));
                receiptIv.setVisibility(View.VISIBLE);
            } else if (chatMessage.sendState == SENT){
                receiptIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_sent));
                receiptIv.setVisibility(View.VISIBLE);
            } else if (chatMessage.sendState == READ){
                receiptIv.setImageDrawable(activity.getDrawable(R.mipmap.tick_read));
                receiptIv.setVisibility(View.VISIBLE);
            } else {
                receiptIv.setVisibility(View.GONE);
            }

            cardMessageCard.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            switch(v.getId()){
                case R.id.chat_message_card:
                    if (v.getParent() instanceof ChatMessageItemLayout) {
                        ChatMessageItemLayout chatMessageItemView = (ChatMessageItemLayout) v.getParent();
                        ChatMessage chatMessage = chatMessageItemView.getChatMessage();
                        if (chatMessage != null) {
                            switch(chatMessage.messageType){
                                case AUDIO:
                                    playAudio(chatMessageItemView, chatMessageItemView.getChatMessage().resourceFile);
                                    break;
                            }
                        }
                    }
                    break;
            }
        }

        private void playAudio(final View view, final ResourceFile resourceFile) {
            AudioPlayManager.getInstance().stopPlay();
            final ImageView audioIv = view.findViewById(R.id.audio_iv);
            final TextView durationTv = view.findViewById(R.id.duration_tv);
            AudioPlayManager.getInstance().startPlay(activity, Uri.parse(resourceFile.filePath), new IAudioPlayListener() {

                long duration = resourceFile.duration;
                private CountDownTimer timer;

                @Override
                public void onStart(Uri var1) {
                    if (audioIv != null && audioIv.getBackground() instanceof AnimationDrawable) {
                        AnimationDrawable animation = (AnimationDrawable) audioIv.getBackground();
                        animation.start();

                        timer = new CountDownTimer(duration*1000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                long seconds = ((duration / 1000) % 60);
                                durationTv.setText(TimeUtils.secondsToString((int)seconds));
                                duration = duration - 1000;
                            }

                            public void onFinish() {
                                duration = resourceFile.duration;
                                long seconds = ((duration / 1000) % 60);
                                durationTv.setText(TimeUtils.secondsToString((int)seconds));
                            }
                        }.start();
                    }
                }

                @Override
                public void onStop(Uri var1) {
                    if (audioIv != null && audioIv.getBackground() instanceof AnimationDrawable) {
                        stop();
                    }

                }

                @Override
                public void onComplete(Uri var1) {
                    if (audioIv != null && audioIv.getBackground() instanceof AnimationDrawable) {
                        stop();
                    }
                }

                private void stop(){
                    AnimationDrawable animation = (AnimationDrawable) audioIv.getBackground();
                    animation.stop();
                    animation.selectDrawable(0);

                    if (timer != null){
                        timer.cancel();
                        timer.onFinish();
                        timer = null;
                    }
                }
            });
        }
    }

    class OtherAudioMessageHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ChatMessageItemLayout rootView;
        private CardView cardMessageCard;
        private ImageView avatarIv;
        private TextView durationTv;
        private TextView dateTv;
        private TextView timeTv;
        private TextView nameTv;

        public OtherAudioMessageHolder(View itemView) {
            super(itemView);
            rootView = (ChatMessageItemLayout) itemView;
            cardMessageCard = itemView.findViewById(R.id.chat_message_card);
            avatarIv = itemView.findViewById(R.id.avatar_iv);
            nameTv = itemView.findViewById(R.id.name_tv);
            durationTv = itemView.findViewById(R.id.duration_tv);
            dateTv = itemView.findViewById(R.id.chat_date_tv);
            timeTv = itemView.findViewById(R.id.chat_time_tv);
        }

        public void bind(final ChatMessage chatMessage, final boolean isNewDay, final boolean isContinuous) {
            rootView.setChatMessage(chatMessage);
            timeTv.setText(TimeUtils.getTimeByTimestamp(chatMessage.createAt));
            ImageLoader.displayImageAsCircle(chatMessage.senderProfileUrl, avatarIv);

            if (isContinuous) {
                nameTv.setVisibility(View.GONE);
            }else{
                nameTv.setVisibility(View.VISIBLE);
                nameTv.setText(chatMessage.senderName);
            }

            long milliseconds = chatMessage.resourceFile.duration;
            long seconds = (long) ((milliseconds / 1000) % 60);
            durationTv.setText(TimeUtils.secondsToString((int)seconds));

            if (isNewDay){
                dateTv.setVisibility(View.VISIBLE);
                dateTv.setText(TimeUtils.getDateStringByTimeStamp(chatMessage.createAt));
            }else{
                dateTv.setVisibility(View.GONE);
            }

            cardMessageCard.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            switch(v.getId()){
                case R.id.chat_message_card:
                    if (v.getParent() instanceof ChatMessageItemLayout) {
                        ChatMessageItemLayout chatMessageItemView = (ChatMessageItemLayout) v.getParent();
                        ChatMessage chatMessage = chatMessageItemView.getChatMessage();
                        if (chatMessage != null) {
                            switch(chatMessage.messageType){
                                case AUDIO:
                                    if (Utils.isBlank(chatMessage.resourceFile.filePath)) {
                                        downloadAudio(chatMessageItemView, chatMessage.resourceFile);
                                    }else{
                                        playAudio(chatMessageItemView, chatMessageItemView.getChatMessage().resourceFile);
                                    }
                                    break;
                            }
                        }
                    }
                    break;
            }
        }

        private void downloadAudio(final View view, final ResourceFile resourceFile){
            ApiServer.baseDownloadFilesRequest(activity, resourceFile.fileUrl, Constants.AUDIO_PATH,
                    resourceFile.fileName, new ApiServer.FileDownloadListener() {
                @Override
                public void onSucceeded(File file) {
                    resourceFile.filePath = file.getPath();
                    resourceFile.save(activity, resourceFile.chatMessageId);

                    playAudio(view, resourceFile);
                }

                @Override
                public void onProgressing(float percentage) {
                    Logger.i("Audio File Dowloading: " + percentage);
                }

                @Override
                public void onFailed(String errorMessage) {
                    ToastUtils.showToast(errorMessage);
                }
            });
        }

        private void playAudio(final View view, final ResourceFile resourceFile) {
            AudioPlayManager.getInstance().stopPlay();
            final ImageView audioIv = view.findViewById(R.id.audio_iv);
            final TextView durationTv = view.findViewById(R.id.duration_tv);
            AudioPlayManager.getInstance().startPlay(activity, Uri.parse(resourceFile.filePath), new IAudioPlayListener() {

                long duration = resourceFile.duration;
                private CountDownTimer timer;

                @Override
                public void onStart(Uri var1) {
                    if (audioIv != null && audioIv.getBackground() instanceof AnimationDrawable) {
                        AnimationDrawable animation = (AnimationDrawable) audioIv.getBackground();
                        animation.start();

                        timer = new CountDownTimer(duration*1000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                long seconds = ((duration / 1000) % 60);
                                durationTv.setText(TimeUtils.secondsToString((int)seconds));
                                duration = duration - 1000;
                            }

                            public void onFinish() {
                                duration = resourceFile.duration;
                                long seconds = ((duration / 1000) % 60);
                                durationTv.setText(TimeUtils.secondsToString((int)seconds));
                            }
                        }.start();
                    }
                }

                @Override
                public void onStop(Uri var1) {
                    if (audioIv != null && audioIv.getBackground() instanceof AnimationDrawable) {
                        stop();
                    }

                }

                @Override
                public void onComplete(Uri var1) {
                    if (audioIv != null && audioIv.getBackground() instanceof AnimationDrawable) {
                        stop();
                    }
                }

                private void stop(){
                    AnimationDrawable animation = (AnimationDrawable) audioIv.getBackground();
                    animation.stop();
                    animation.selectDrawable(0);

                    if (timer != null){
                        timer.cancel();
                        timer.onFinish();
                        timer = null;
                    }
                }
            });
        }
    }
}















