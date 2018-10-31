package ekoolab.com.show.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicInteger;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.ChatActivity;
import ekoolab.com.show.activities.MainActivity;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Chat.ChatManager;
import ekoolab.com.show.utils.Utils;

public class ShowFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private final static AtomicInteger notificationID = new AtomicInteger(0);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            String senderId = null;
            String message = remoteMessage.getData().get("message");
            try {
                JSONObject sendBird = new JSONObject(remoteMessage.getData().get("sendbird"));
                senderId = sendBird.getJSONObject("sender").getString("id");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendNotification(message, senderId);
        }

    }

    @Override
    public void onNewToken(String token) {
        sendRegistrationToServer();
    }

    private void sendRegistrationToServer() {
        AuthUtils.AuthType authType = AuthUtils.getInstance(this).loginState();
        String apiToken = AuthUtils.getInstance(this).getApiToken();
        if (authType == AuthUtils.AuthType.LOGGED && !Utils.isBlank(apiToken)){
            ChatManager.getInstance(this).registerDeviceTokenWithSBird();
        }
    }

    private void sendNotification(String messageBody, String sendId) {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        final String CHANNEL_ID = "SHOW_CHAT_CHANNEL_ID";
        final String CHANNEL_NAME = "CHAT";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }

        Friend friend = Friend.getFriendByUserCode(this, sendId);
        if(friend != null){
            Intent mainIntent = new Intent (this, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(mainIntent);

            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra(ChatActivity.FRIEND_DATA, friend);
            stackBuilder.addNextIntent(chatIntent);

            PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(this.getResources().getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setPriority(Notification.PRIORITY_MAX)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(pendingIntent);
            notificationBuilder.setContentText(messageBody);
            notificationManager.notify(ShowFirebaseMessagingService.notificationID.incrementAndGet(), notificationBuilder.build());
        }
    }
}
