package ekoolab.com.show.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.FileMessage;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.ChatActionBtnAdapter;
import ekoolab.com.show.adapters.ChatMessageAdapter;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.ChatMessage;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.beans.ResourceFile;
import ekoolab.com.show.beans.enums.FileType;
import ekoolab.com.show.beans.enums.MessageType;
import ekoolab.com.show.beans.enums.SendState;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Chat.ChatManager;
import ekoolab.com.show.utils.Chat.ChatMessageItemLayout;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.AudioView.AudioRecordManager;
import ekoolab.com.show.views.AudioView.IAudioRecordListener;
import ekoolab.com.show.views.ChatRecyclerView;
import ekoolab.com.show.views.itemdecoration.GridSpacingItemDecoration;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class ChatActivity extends BaseActivity implements View.OnTouchListener, TextWatcher,
        View.OnClickListener,
        TextView.OnEditorActionListener,
        ViewTreeObserver.OnGlobalLayoutListener, ChatManager.ChatManagerListener {
    public static final String FRIEND_DATA = "friend_data";

    private final int spanCount = 4;
    private RelativeLayout rootView;
    private View contentView;
    private RecyclerView recyclerView;
    private RelativeLayout editorBar;
    private RecyclerView moreActionRecycleView;
    private ImageButton voiceBtn;
    private ImageButton moreBtn;
    private Button recordBtn;
    private EditText messageEt;
    private TextView nameTv;

    private Friend friend;
    private List<Action> actions;

    private int offset;
    private int limit;

    private int moveSpace = 0;
    private float startX = 0;
    private float startY = 0;
    private float stopX = 0;
    private float stopY = 0;
    private int mPreviousHeight;
    private List<ChatMessage> chatMessages;
    private Map<String, ChatMessage> tempChatMessageMap;

    private boolean audioGranted;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initData() {
        super.initData();
        offset = 0;
        limit = 20;
        audioGranted = false;
        initActions();
        friend = getIntent().getParcelableExtra(FRIEND_DATA);
        chatMessages = new ArrayList<>();
        tempChatMessageMap = new HashMap<>();
        chatMessages.addAll(getChatMessageFromDB());

        rxPermissions.request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(isGranted -> {
                    audioGranted = isGranted;
                });

        ChatManager.getInstance(this).register(this);
        AudioRecordManager.getInstance(this).setMaxVoiceDuration(60);
        AudioRecordManager.getInstance(this).setAudioSavePath(Constants.AUDIO_PATH);
    }

    @Override
    protected void initViews() {
        super.initViews();

        rootView = findViewById(R.id.root_view);
        contentView = findViewById(android.R.id.content);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        editorBar = findViewById(R.id.editor_bar);

        nameTv = findViewById(R.id.name_tv);
        nameTv.setText(Utils.getDisplayName(friend.name, friend.nickName));

        recyclerView = findViewById(R.id.message_rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new LinearItemDecoration(this,
                0, R.color.colorLightGray, 0));
        recyclerView.setAdapter(new ChatMessageAdapter(this, chatMessages));
        recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startY = e.getY();
                        startX = e.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        stopX = e.getX();
                        stopY = e.getY();

                        View item = rv.findChildViewUnder(stopX, stopY);
                        if (item instanceof ChatMessageItemLayout) {
                            ChatMessage chatMessage = ((ChatMessageItemLayout) item).getChatMessage();
                            if(chatMessage.messageType == MessageType.AUDIO){
                                CardView messageCardView = item.findViewById(R.id.chat_message_card);
                                int[] location = new int[2];
                                messageCardView.getLocationOnScreen(location);

                                int messageCardViewX = location[0];
                                int messageCardViewY = location[1];

                                float touchX = e.getX();
                                float touchY = e.getY();

                                if (Utils.equals(AuthUtils.getInstance(ChatActivity.this).getUserCode(), chatMessage.senderId)){ // outgoing
                                    if(touchX > messageCardViewX && touchX < messageCardViewX + messageCardView.getMeasuredWidth()
                                            && touchY > messageCardViewY && touchY <  touchY + messageCardView.getMeasuredHeight()){
                                        return false;
                                    }
                                }
                            }
                        }

                        if ((Math.abs(stopX - startX) < 10 && Math.abs(stopY - startY) < 10)) {
                            Utils.hideInput(messageEt);
                            if (moreActionRecycleView.getTranslationY() == 0) {
                                hideMoreAction(true);
                            }
                        }
                        break;
                }

                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        messageEt = findViewById(R.id.message_et);
        messageEt.addTextChangedListener(this);
        messageEt.setOnEditorActionListener(this);

        moreBtn = findViewById(R.id.more_btn);
        moreBtn.setOnClickListener(this);

        voiceBtn = findViewById(R.id.voice_btn);
        voiceBtn.setOnClickListener(this);

        recordBtn = findViewById(R.id.record_btn);
        recordBtn.setOnTouchListener(this);

        moreActionRecycleView = findViewById(R.id.more_action_rv);
        moreActionRecycleView.setLayoutManager(new GridLayoutManager(this, spanCount));
        moreActionRecycleView.addItemDecoration(new GridSpacingItemDecoration(spanCount, DisplayUtils.dip2px(20), true));
        moreActionRecycleView.post(new Runnable() {
            @Override
            public void run() {
                moveSpace =  moreActionRecycleView.getMeasuredHeight();
                ObjectAnimator.ofFloat(moreActionRecycleView, "translationY", moveSpace).setDuration(0).start();
            }
        });

        moreActionRecycleView.setAdapter(new ChatActionBtnAdapter(this, actions));

        setAudioRecordListener();
    }

    private void setAudioRecordListener(){
        AudioRecordManager.getInstance(this).setAudioRecordListener(new IAudioRecordListener() {

            private TextView mTimerTV;
            private TextView mStateTV;
            private ImageView mStateIV;
            private PopupWindow mRecordWindow;

            @Override
            public void initTipView() {
                View view = View.inflate(ChatActivity.this, R.layout.dialog_audio_info, null);
                mStateIV = view.findViewById(R.id.rc_audio_state_image);
                mStateTV = view.findViewById(R.id.rc_audio_state_text);
                mTimerTV = view.findViewById(R.id.rc_audio_timer);
                mRecordWindow = new PopupWindow(view, -1, -1);
                mRecordWindow.showAtLocation(rootView, 17, 0, 0);
                mRecordWindow.setFocusable(true);
                mRecordWindow.setOutsideTouchable(false);
                mRecordWindow.setTouchable(false);
            }

            @Override
            public void setTimeoutTipView(int counter) {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility(View.GONE);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.mipmap.bg_voice_popup);
                    this.mTimerTV.setText(String.format("%s", new Object[]{Integer.valueOf(counter)}));
                    this.mTimerTV.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void setRecordingTipView() {
                if (this.mRecordWindow != null) {
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_rec);
                    this.mStateTV.setBackgroundResource(R.mipmap.bg_voice_popup);
                    this.mTimerTV.setVisibility(View.GONE);
                }
            }

            @Override
            public void setAudioShortTipView() {
                if (this.mRecordWindow != null) {
                    mStateIV.setImageResource(R.mipmap.ic_volume_wraning);
                    mStateTV.setText(R.string.voice_short);
                }
            }

            @Override
            public void setCancelTipView() {
                if (this.mRecordWindow != null) {
                    this.mTimerTV.setVisibility(View.GONE);
                    this.mStateIV.setVisibility(View.VISIBLE);
                    this.mStateIV.setImageResource(R.mipmap.ic_volume_cancel);
                    this.mStateTV.setVisibility(View.VISIBLE);
                    this.mStateTV.setText(R.string.voice_cancel);
                    this.mStateTV.setBackgroundResource(R.drawable.bg_corner_voice);
                }
            }

            @Override
            public void destroyTipView() {
                if (this.mRecordWindow != null) {
                    this.mRecordWindow.dismiss();
                    this.mRecordWindow = null;
                    this.mStateIV = null;
                    this.mStateTV = null;
                    this.mTimerTV = null;
                }
            }

            @Override
            public void onStartRecord() {
                //开始录制
            }

            @Override
            public void onFinish(Uri audioPath, int duration) {
                //发送文件
                File file = new File(audioPath.getPath());
                if (file.exists()) {
                    ChatMessage tempChatMessage = ChatMessage.createTempChatMessage(getBaseContext(), file, FileType.AUDIO);
                    tempChatMessage.senderId = AuthUtils.getInstance(ChatActivity.this).getUserCode();
                    chatMessages.add(tempChatMessage);
                    recyclerView.getAdapter().notifyDataSetChanged();

                    tempChatMessageMap.put(audioPath.getPath(), tempChatMessage);
                    uploadAudio(file);
                }
            }

            @Override
            public void onAudioDBChanged(int db) {
                switch (db / 5) {
                    case 0:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_1);
                        break;
                    case 1:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_2);
                        break;
                    case 2:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_3);
                        break;
                    case 3:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_4);
                        break;
                    case 4:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_5);
                        break;
                    case 5:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_6);
                        break;
                    case 6:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_7);
                        break;
                    default:
                        this.mStateIV.setImageResource(R.mipmap.ic_volume_8);
                }
            }
        });
    }

    private List<ChatMessage> getChatMessageFromDB(){
        List<ChatMessage> dbChatMessages = ChatMessage
                .getChatMessages(this, Constants.ChatMessageTableColumns.channelUrl + "=?",
                        new String[]{friend.channelUrl}, offset + "," + limit);

        return dbChatMessages;
    }

    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth() || event.getRawY() < location[1] - 40) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view == recordBtn){
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    AudioRecordManager.getInstance(this).startRecord();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (isCancelled(view, event)) {
                        AudioRecordManager.getInstance(this).willCancelRecord();
                    } else {
                        AudioRecordManager.getInstance(this).continueRecord();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    AudioRecordManager.getInstance(this).stopRecord();
                    AudioRecordManager.getInstance(this).destroyRecord();
                    break;
            }
        }
        return true;
    }
    //true can click but cannot scroll

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.more_btn:
                if (moreActionRecycleView.getTranslationY() > 0) {
                    showMoreAction();
                }else{
                    hideMoreAction(true);
                }
                break;
            case R.id.voice_btn:
                if (!audioGranted) {
                    toastLong(getString(R.string.audio_permission_error));
                    return;
                }
                if (recordBtn.getVisibility() == View.VISIBLE){
                    recordBtn.setVisibility(View.GONE);
                    messageEt.setVisibility(View.VISIBLE);
                }else{
                    recordBtn.setVisibility(View.VISIBLE);
                    messageEt.setVisibility(View.GONE);
                }

                break;
        }
    }

    @Override
    public void onGlobalLayout() {
        int newHeight = contentView.getHeight();
        if (mPreviousHeight != 0) {
            if (mPreviousHeight > newHeight) { //keyboard show up
                if (editorBar.getTranslationY() < 0) {
                    hideMoreAction(false);
                }
            }
        }
        mPreviousHeight = newHeight;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if(i== EditorInfo.IME_ACTION_SEND) {
            sendMessage();
        }
        return false;
    }

    private void sendMessage(){
        String messageBody = messageEt.getText().toString().trim();
        if(Utils.isBlank(messageBody)){
            toastLong(getString(R.string.message_empty));
            return;
        }

        UserMessage userMessage = ChatManager.getInstance(this).sendMessage(messageBody, friend.userCode, MessageType.TEXT, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                ListIterator<ChatMessage> listIterator = chatMessages.listIterator(chatMessages.size());
                while(listIterator.hasPrevious()){
                    ChatMessage chatMessage = listIterator.previous();
                    if (Utils.equals(chatMessage.requestId, userMessage.getRequestId())) {
                        chatMessage.messageId = userMessage.getMessageId();
                        chatMessage.senderId = AuthUtils.getInstance(ChatActivity.this).getUserCode();
                        chatMessage.sendState = SendState.SENT;
                        chatMessage.save(ChatActivity.this);

                        int position = chatMessages.indexOf(chatMessage);
                        recyclerView.getAdapter().notifyItemChanged(position);
                        break;
                    }
                }
            }
        });

        ChatMessage chatMessage = ChatMessage.createByOutgoingUserMessage(this, userMessage);
        chatMessage.save(this);
        chatMessages.add(chatMessage);

        recyclerView.getAdapter().notifyDataSetChanged();
        messageEt.setText("");
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount()-1);
    }

    private void sendAudioMessage(final ResourceFile resourceFile, MessageType messageType){
        FileMessage fileMessage = ChatManager.getInstance(this).sendFileMessage(resourceFile, friend.userCode, messageType, new BaseChannel.SendFileMessageHandler() {
            @Override
            public void onSent(FileMessage fileMessage, SendBirdException e) {
                ListIterator<ChatMessage> listIterator = chatMessages.listIterator(chatMessages.size());
                while(listIterator.hasPrevious()){
                    ChatMessage chatMessage = listIterator.previous();
                    if (chatMessage.isTemp && chatMessage.resourceFile != null){
                        if(tempChatMessageMap.containsKey(chatMessage.resourceFile.filePath)) {
                            ChatMessage tempChatMessage = tempChatMessageMap.get(chatMessage.resourceFile.filePath);
                            tempChatMessage.messageId = fileMessage.getMessageId();
                            tempChatMessage.sendState = SendState.SENT;
                            tempChatMessage.save(ChatActivity.this);

                            int position = chatMessages.indexOf(chatMessage);
                            chatMessages.set(position, tempChatMessage);
                            recyclerView.getAdapter().notifyItemChanged(position);
                            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount()-1);

                            tempChatMessageMap.remove(chatMessage.resourceFile.filePath);
                            break;
                        }
                    }
                }
            }
        });

        ChatMessage chatMessage = ChatMessage.createByOutgoingFileMessage(this, fileMessage, resourceFile);
        if (tempChatMessageMap.containsKey(resourceFile.filePath)){
            tempChatMessageMap.put(resourceFile.filePath, chatMessage);
        }
    }

    private void uploadAudio(final File audioFile){
        HashMap<String, String> params = new HashMap<>();
        params.put("token", AuthUtils.getInstance(this).getApiToken());

        HashMap<String, File> fileParams = new HashMap<>();
        fileParams.put("audio", audioFile);

        ApiServer.baseUploadRequest(this, Constants.UPLOAD_AUDIO, params, fileParams, new TypeToken<ResponseData<Map<String, String>>>(){
        })
        .subscribe(new NetworkSubscriber<Map<String, String>>() {
            @Override
            protected void onSuccess(Map<String, String> data) {
                String audioUrl = data.get("audioUri");

                ChatMessage tempChatMessage = tempChatMessageMap.get(audioFile.getPath());
                tempChatMessage.resourceFile.fileUrl = audioUrl;
                sendAudioMessage(tempChatMessage.resourceFile, tempChatMessage.messageType);

            }

            @Override
            protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                Logger.i("Uoload audio file error: " + errorMsg);
                return super.dealHttpException(code, errorMsg, e);
            }
        });
    }

    @Override
    public void didReceivedMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount()-1);
    }

    private void showMoreAction(){
        Utils.hideInput(messageEt);
        ObjectAnimator.ofFloat(recyclerView, "translationY", -moveSpace).start();
        ObjectAnimator.ofFloat(editorBar, "translationY", -moveSpace).start();
        ObjectAnimator.ofFloat(moreActionRecycleView, "translationY", 0).start();
    }

    private void hideMoreAction(boolean animated){
        long duration = 300;
        if (!animated){
            duration = 0;
        }
        ObjectAnimator.ofFloat(recyclerView, "translationY", 0).setDuration(duration).start();
        ObjectAnimator.ofFloat(editorBar, "translationY", 0).setDuration(duration).start();
        ObjectAnimator.ofFloat(moreActionRecycleView, "translationY", moveSpace).setDuration(duration).start();
    }

    private void initActions() {
        actions = new ArrayList<>();
        actions.add(new Action(R.drawable.selector_voice_call_btn, R.string.voice_call));
        actions.add(new Action(R.drawable.selector_video_call_btn, R.string.video_call));
    }

    public class Action {
        public int iconId;
        public int titleId;

        public Action(int iconId, int titleId) {
            this.iconId = iconId;
            this.titleId = titleId;
        }
    }

}

































