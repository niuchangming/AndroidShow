package ekoolab.com.show.adapters;

import android.content.Context;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.ChatActivity;

public class ChatActionBtnAdapter extends BaseQuickAdapter<ChatActivity.Action, BaseViewHolder> {
    private Context context;
    private List<ChatActivity.Action> actions;

    public ChatActionBtnAdapter(Context context, @Nullable List<ChatActivity.Action> data) {
        super(R.layout.item_chat_action_btn, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, ChatActivity.Action item) {
        helper.setImageResource(R.id.icon_iv, item.iconId);
        helper.setText(R.id.name_tv, context.getString(item.titleId));
    }
}
