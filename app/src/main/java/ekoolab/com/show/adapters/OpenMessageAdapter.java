package ekoolab.com.show.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.UserMessage;

import java.util.List;

import ekoolab.com.show.R;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Utils;

public class OpenMessageAdapter extends RecyclerView.Adapter <RecyclerView.ViewHolder> {

    private static final int OPEN_MESSAGE_ME = 1000;
    private static final int OPEN_MESSAGE_OTHER = 1001;
    private static final int OPEN_MESSAGE_ADMIN = 1002;

    private Activity activity;
    private List<BaseMessage> openMessages;

    public OpenMessageAdapter(Activity activity, List<BaseMessage> openMessages) {
        this.activity = activity;
        this.openMessages = openMessages;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case OPEN_MESSAGE_ME:
                View openMeView = activity.getLayoutInflater().inflate(R.layout.item_open_outgoing_message, parent, false);
                return new OpenMessageAdapter.OpenMeMessageHolder(openMeView);
            case OPEN_MESSAGE_OTHER:
                View openOtherView = activity.getLayoutInflater().inflate(R.layout.item_open_incoming_message, parent, false);
                return new OpenMessageAdapter.OpenOtherMessageHolder(openOtherView);
            case OPEN_MESSAGE_ADMIN:
                View openIncomingView = activity.getLayoutInflater().inflate(R.layout.item_open_incoming_admin, parent, false);
                return new OpenMessageAdapter.OpenAdminMessageHolder(openIncomingView);
            default:
                return null;

        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseMessage baseMessage = openMessages.get(position);

        switch (holder.getItemViewType()) {
            case OPEN_MESSAGE_ME:
                ((OpenMeMessageHolder) holder).bind((UserMessage) baseMessage);
                break;
            case OPEN_MESSAGE_OTHER:
                ((OpenOtherMessageHolder) holder).bind((UserMessage) baseMessage);
                break;
            case OPEN_MESSAGE_ADMIN:
                ((OpenAdminMessageHolder) holder).bind((AdminMessage) baseMessage);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        BaseMessage baseMessage = openMessages.get(position);

        String currentUserId = Utils.isBlank(AuthUtils.getInstance(activity).getUserCode()) ?
                AuthUtils.getInstance(activity).getTempUserId() : AuthUtils.getInstance(activity).getUserCode();

        if(baseMessage instanceof UserMessage){
            UserMessage userMessage = (UserMessage) baseMessage;
            if(Utils.equals(currentUserId, userMessage.getSender().getUserId())) {
                return OPEN_MESSAGE_ME;
            }else{
                return OPEN_MESSAGE_OTHER;
            }
        }else if(baseMessage instanceof AdminMessage){
            AdminMessage adminMessage = (AdminMessage) baseMessage;
            return OPEN_MESSAGE_ADMIN;
        }

        return OPEN_MESSAGE_OTHER;
    }

    @Override
    public int getItemCount() {
        return openMessages.size();
    }

    class OpenMeMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageTv;

        public OpenMeMessageHolder(View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.message_tv);
        }

        public void bind(final UserMessage userMessage) {
            messageTv.setText(userMessage.getMessage());
        }
    }

    class OpenOtherMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageTv;
        private TextView nameTv;

        public OpenOtherMessageHolder(View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.message_tv);
            nameTv = itemView.findViewById(R.id.name_tv);
        }

        public void bind(final UserMessage userMessage) {
            messageTv.setText(userMessage.getMessage());
            nameTv.setText(userMessage.getSender().getNickname() + ":");
        }
    }

    class OpenAdminMessageHolder extends RecyclerView.ViewHolder {
        private TextView messageTv;

        public OpenAdminMessageHolder(View itemView) {
            super(itemView);

            messageTv = itemView.findViewById(R.id.message_tv);
        }

        public void bind(final AdminMessage adminMessage) {
            messageTv.setText(adminMessage.getMessage());
        }
    }
}
