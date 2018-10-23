package ekoolab.com.show.activities;

import android.animation.ObjectAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.orhanobut.logger.Logger;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.ChatActionBtnAdapter;
import ekoolab.com.show.adapters.ChatMessageAdapter;
import ekoolab.com.show.beans.ChatMessage;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.beans.enums.MessageType;
import ekoolab.com.show.utils.Chat.ChatManager;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.itemdecoration.GridSpacingItemDecoration;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class ChatActivity extends BaseActivity implements View.OnTouchListener, TextWatcher,
        View.OnClickListener,
        TextView.OnEditorActionListener,
        ViewTreeObserver.OnGlobalLayoutListener, ChatManager.ChatManagerListener {
    public static final String FRIEND_DATA = "friend_data";

    private final int spanCount = 4;
    private View contentView;
    private RecyclerView recyclerView;
    private RelativeLayout editorBar;
    private RecyclerView moreActionRecycleView;
    private ImageButton voiceBtn;
    private ImageButton moreBtn;
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

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initData() {
        super.initData();
        offset = 0;
        limit = 20;
        initActions();
        friend = getIntent().getParcelableExtra(FRIEND_DATA);
        chatMessages = new ArrayList<>();
        chatMessages.addAll(getChatMessageFromDB());

        ChatManager.getInstance(this).register(this);
    }

    @Override
    protected void initViews() {
        super.initViews();

        contentView = findViewById(android.R.id.content);
        contentView.getViewTreeObserver().addOnGlobalLayoutListener(this);

        editorBar = findViewById(R.id.editor_bar);

        nameTv = findViewById(R.id.name_tv);
        nameTv.setText(Utils.getDisplayName(friend.name, friend.nickName));

        recyclerView = findViewById(R.id.message_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LinearItemDecoration(this,
                0, R.color.colorLightGray, 0));
        recyclerView.setOnTouchListener(this);
        recyclerView.setAdapter(new ChatMessageAdapter(this, chatMessages));

        messageEt = findViewById(R.id.message_et);
        messageEt.addTextChangedListener(this);
        messageEt.setOnEditorActionListener(this);

        moreBtn = findViewById(R.id.more_btn);
        moreBtn.setOnClickListener(this);

        voiceBtn = findViewById(R.id.voice_btn);
        voiceBtn.setOnClickListener(this);

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
    }

    private List<ChatMessage> getChatMessageFromDB(){
        List<ChatMessage> dbChatMessages = ChatMessage
                .getChatMessages(this, Constants.ChatMessageTableColumns.channelUrl + "=?",
                        new String[]{friend.channelUrl}, offset + "," + limit);

        return dbChatMessages;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startY = event.getY();
                startX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                stopX = event.getX();
                stopY = event.getY();

                if (Math.abs(stopX - startX) < 10 && Math.abs(stopY - startY) < 10) {
                    Utils.hideInput(messageEt);
                    if (moreActionRecycleView.getTranslationY() == 0) {
                        hideMoreAction(true);
                    }
                }
                break;
        }
        return true;
    }

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

            }
        });

        ChatMessage chatMessage = ChatMessage.createByOutgoing(this, userMessage);
        chatMessage.save(this);
        chatMessages.add(chatMessage);

        recyclerView.getAdapter().notifyDataSetChanged();
        messageEt.setText("");
    }

    @Override
    public void didReceivedMessage(ChatMessage chatMessage) {
        chatMessages.add(chatMessage);
        recyclerView.getAdapter().notifyDataSetChanged();
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

































