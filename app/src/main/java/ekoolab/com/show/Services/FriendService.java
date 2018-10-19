package ekoolab.com.show.Services;

import android.app.Service;
import android.arch.lifecycle.Lifecycle;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.luck.picture.lib.tools.Constant;
import com.orhanobut.logger.Logger;
import com.rx2androidnetworking.Rx2AndroidNetworking;
import com.uber.autodispose.AutoDispose;
import com.uber.autodispose.AutoDisposeConverter;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.beans.Friend;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;
import ekoolab.com.show.utils.LocalBinder;
import ekoolab.com.show.utils.Utils;

public class FriendService extends Service {
    public static final String CONTACT_UPLOADED = "ekoolab.com.show.contact_updated";
    public static final String CONTACT_LAST_SYNC_TIME = "ekoolab.com.show.contact_last_sync_time";
    private Map<String, Friend> localFriendMap;
    private long lastQueryTime;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (localFriendMap.size() > 0) {
                uploadContactToServer(new ArrayList<>(localFriendMap.values()));
            }else{
                getContactFromServer();
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<>(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sp = this.getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        lastQueryTime = sp.getLong(CONTACT_LAST_SYNC_TIME, 0);

        new Thread(new Runnable() {
            @Override
            public void run() {

                localFriendMap = getLocalContactMap();
                if(localFriendMap.size() > 0){
                    List<Friend> friendList = new ArrayList<>(localFriendMap.values());
                    Friend.batchSave(FriendService.this, friendList);

                    SharedPreferences.Editor spEditor = sp.edit();
                    spEditor.putLong(CONTACT_LAST_SYNC_TIME, System.currentTimeMillis());
                    spEditor.commit();
                }

                handler.sendEmptyMessage(0);
            }
        }).start();
    }

    public Map<String, Friend> getLocalContactMap(){
        Map<String, Friend> localContacts = new HashMap<>();
        Cursor cursor;

        if(lastQueryTime == 0 || android.os.Build.MANUFACTURER.equals("alps")){
            cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        }else{
            cursor = this.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Contactables.CONTACT_LAST_UPDATED_TIMESTAMP + " > ?",
                    new String[]{"" + lastQueryTime}, null);
        }

        if(cursor != null){
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if(Utils.isBlank(phone) || localContacts.containsKey(phone)){
                    continue;
                }

                String defaultCountryCode = "SG";
                if (phone.trim().length() > 10) {
                    defaultCountryCode = "CN";
                }

                Phonenumber.PhoneNumber phoneNumber = null;
                try {
                    phoneNumber = PhoneNumberUtil.getInstance().parse(Utils.formatMobile(phone, defaultCountryCode), defaultCountryCode);
                }catch (NumberParseException e){
                    e.printStackTrace();
                }

                Friend friend = new Friend();
                friend.name = displayName;

                if(phoneNumber == null){
                    friend.countryCode = "65";
                    friend.mobile = phone;
                }else{
                    friend.mobile = phoneNumber.getNationalNumber() + "";
                    friend.countryCode = phoneNumber.getCountryCode() + "";
                }
                localContacts.put(phone, friend);
            }
            cursor.close();
        }
        return localContacts;
    }

    public void uploadContactToServer(List<Friend> friends){

        List<Map<String, String>> friendData = new ArrayList<>();
        for(Friend friend : friends){
            Map<String, String> friendMap = new HashMap<String, String>();
            friendMap.put("name", Utils.getDisplayName(friend.name, friend.nickName));
            friendMap.put("countryCode", friend.countryCode);
            friendMap.put("mobile", friend.mobile);
            friendData.add(friendMap);
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("contacts", friendData);
        map.put("token", AuthUtils.getInstance(getBaseContext()).getApiToken());
        map.put("userCode", AuthUtils.getInstance(getBaseContext()).getUserCode());

        ApiServer.basePostRequestNoDisposable(Constants.UPLOAD_CONTACT_BOOK, map,
                new TypeToken<ResponseData<List<Friend>>>() {
                })
                .subscribe(new NetworkSubscriber<List<Friend>>() {
                    @Override
                    protected void onSuccess(List<Friend> friends) {
                        Logger.i("----------> " + friends.size());

                        getContactFromServer();
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        Logger.i(errorMsg);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });

    }

    private void getContactFromServer() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("token", AuthUtils.getInstance(this).getApiToken());
        ApiServer.basePostRequestNoDisposable(Constants.FRIENDS, map,
                new TypeToken<ResponseData<List<Friend>>>(){
                })
                .subscribe(new NetworkSubscriber<List<Friend>>(){

                    @Override
                    protected void onSuccess(List<Friend> friends) {

                        for (Friend friend : friends) {
                            friend.isAppUser = true;
                        }

                        Friend.batchSave(FriendService.this, friends);

                        Intent intent = new Intent();
                        intent.setAction(FriendService.CONTACT_UPLOADED);
                        FriendService.this.sendBroadcast(intent);
                    }

                });
    }

}












