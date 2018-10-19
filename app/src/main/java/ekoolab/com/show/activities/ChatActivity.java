package ekoolab.com.show.activities;

import android.animation.ObjectAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.adapters.ChatActionBtnAdapter;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.utils.DisplayUtils;
import ekoolab.com.show.utils.Utils;
import ekoolab.com.show.views.itemdecoration.GridSpacingItemDecoration;
import ekoolab.com.show.views.itemdecoration.LinearItemDecoration;

public class ChatActivity extends BaseActivity implements View.OnTouchListener, TextWatcher, View.OnClickListener {
    public static final String FRIEND_DATA = "friend_data";

    private final int spanCount = 4;
    private RecyclerView recyclerView;
    private RelativeLayout rootView;
    private RelativeLayout editorBar;
    private RecyclerView moreActionRecycleView;
    private ImageButton voiceBtn;
    private ImageButton moreBtn;
    private EditText messageEt;
    private TextView nameTv;

    private Friend friend;
    private List<Action> actions;

    private float startX = 0;
    private float startY = 0;
    private float stopX = 0;
    private float stopY = 0;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat;
    }

    @Override
    protected void initData() {
        super.initData();

        initActions();
        friend = getIntent().getParcelableExtra(FRIEND_DATA);
    }

    @Override
    protected void initViews() {
        super.initViews();

        rootView = findViewById(R.id.root_view);
        editorBar = findViewById(R.id.editor_bar);

        nameTv = findViewById(R.id.name_tv);
        nameTv.setText(Utils.getDisplayName(friend.name, friend.nickName));

        recyclerView = findViewById(R.id.message_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new LinearItemDecoration(this,
                0, R.color.gray_very_light, 0));
        recyclerView.setOnTouchListener(this);

        messageEt = findViewById(R.id.message_et);
        messageEt.addTextChangedListener(this);

        moreBtn = findViewById(R.id.more_btn);
        moreBtn.setOnClickListener(this);

        voiceBtn = findViewById(R.id.voice_btn);
        voiceBtn.setOnClickListener(this);

        moreActionRecycleView = findViewById(R.id.more_action_rv);
        moreActionRecycleView.setLayoutManager(new GridLayoutManager(this, spanCount));
        moreActionRecycleView.addItemDecoration(new GridSpacingItemDecoration(spanCount, DisplayUtils.dip2px(20), true));
//        moreActionRecycleView.post(new Runnable() {
//            @Override
//            public void run() {
//                ObjectAnimator.ofFloat(moreActionRecycleView, "translationY", moreActionRecycleView.getMeasuredHeight()).setDuration(0).start();
//            }
//        });

        moreActionRecycleView.setAdapter(new ChatActionBtnAdapter(this, actions));
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

































