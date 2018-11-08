package ekoolab.com.show.utils.Chat;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import ekoolab.com.show.beans.ChatMessage;

public class ChatMessageItemLayout extends RelativeLayout{

    private ChatMessage chatMessage;

    public ChatMessageItemLayout(Context context) {
        super(context);
    }

    public ChatMessageItemLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatMessage getChatMessage() {
        return chatMessage;
    }

    public void setChatMessage(ChatMessage chatMessage) {
        this.chatMessage = chatMessage;
    }
}
