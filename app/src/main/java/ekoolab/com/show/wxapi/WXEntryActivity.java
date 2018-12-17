package ekoolab.com.show.wxapi;

import android.content.Intent;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.google.gson.reflect.TypeToken;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.activities.SMSLoginActivity;
import ekoolab.com.show.api.ApiServer;
import ekoolab.com.show.api.NetworkSubscriber;
import ekoolab.com.show.api.ResponseData;
import ekoolab.com.show.application.ShowApplication;
import ekoolab.com.show.beans.LoginData;
import ekoolab.com.show.utils.AuthUtils;
import ekoolab.com.show.utils.Constants;

public class WXEntryActivity extends BaseActivity implements IWXAPIEventHandler{
    public static final String WX_LOGIN_STARTED = "ekoolab.com.show.wx.login.started";
    public static final String WX_LOGIN_ENDED = "ekoolab.com.show.wx.login.ended";

    @Override
    protected void initData() {
        super.initData();
        ShowApplication.iwxapi.handleIntent(getIntent(), this);
    }


    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == 1){
            SendAuth.Resp resp = (SendAuth.Resp) baseResp;
            switch (resp.errCode) {
                case BaseResp.ErrCode.ERR_OK:
                    String code = String.valueOf(resp.code);
                    //获取用户信息
                    getAccessToken(code);
                    break;
                case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权
                    toastLong(getString(R.string.wx_login_reject));
                    break;
                case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                    toastLong(getString(R.string.wx_login_cancel));
                    break;
                default:
                    break;
            }
        }
    }

    private void getAccessToken(String code) {
        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + Constants.WECHAT_APP_ID + "&secret=" + Constants.WECHAT_SECRET + "&code=" + code + "&grant_type=authorization_code";

        sendBroadcast(WX_LOGIN_STARTED, null);

        AndroidNetworking.get(url).build().getAsJSONObject(new JSONObjectRequestListener() {
            @Override
            public void onResponse(JSONObject responseMap) {
                try {
                    String accessToken = responseMap.getString("access_token");
                    String openId = responseMap.getString("openid");
                    String unionId = responseMap.getString("unionid");
                    long expiredIn = responseMap.getLong("expires_in");
                    String refreshToken = responseMap.getString("refresh_token");

                    afterWeChatLogin(accessToken, openId, unionId, expiredIn, refreshToken);
                }catch(JSONException e){
                    sendBroadcast(WX_LOGIN_ENDED, null);
                }
            }

            @Override
            public void onError(ANError anError) {
                sendBroadcast(WX_LOGIN_ENDED, null);
            }
        });
    }

    private void afterWeChatLogin(final String accessToken, String openId, String unionId, long expiredIn, String refreshToken) {
        HashMap<String, Object> map = new HashMap<>(4);
        map.put("type", "wechat");
        map.put("access_token", accessToken);
        map.put("expired", expiredIn + "");
        map.put("union_id", unionId);
        map.put("open_id", openId);
        map.put("refresh_token", refreshToken);
        ApiServer.basePostRequestNoDisposable(Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>(){})
                .subscribe(new NetworkSubscriber<LoginData>() {
                    @Override
                    protected void onSuccess(LoginData loginData) {
                        AuthUtils.getInstance(getApplicationContext()).saveLoginInfo(loginData);
                        sendBroadcast(WX_LOGIN_ENDED, loginData);
                    }

                    @Override
                    protected boolean dealHttpException(int code, String errorMsg, Throwable e) {
                        sendBroadcast(WX_LOGIN_ENDED, null);
                        return super.dealHttpException(code, errorMsg, e);
                    }
                });
    }

    private void sendBroadcast(String identifier, LoginData loginData){
        Intent msgIntent = new Intent();
        if(loginData != null){
            msgIntent.putExtra(SMSLoginActivity.LOGIN_DATA, loginData);
        }
        msgIntent.setAction(identifier);
        this.sendBroadcast(msgIntent);
        this.finish();
    }


    @Override
    protected int getLayoutId() {
        return 0;
    }
}
























































