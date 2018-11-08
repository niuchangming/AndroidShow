package ekoolab.com.show.wxapi;

import android.content.Intent;

import com.facebook.login.LoginResult;
import com.google.gson.reflect.TypeToken;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;

import java.util.HashMap;
import java.util.Map;

import ekoolab.com.show.R;
import ekoolab.com.show.activities.BaseActivity;
import ekoolab.com.show.activities.LoginActivity;
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
        ApiServer.baseGetRequest(this, url, new TypeToken<ResponseData<Map<String, String>>>(){})
                .subscribe(new NetworkSubscriber<Map<String, String>>(){

                    @Override
                    protected void onSuccess(Map<String, String> responseMap) {
                        String accessToken = responseMap.get("acces_token");
                        String openId = responseMap.get("openid");
                        String unionId = responseMap.get("unionid");
                        long expiredIn = Long.parseLong(responseMap.get("expires_in"));
                        String refreshToken = responseMap.get("refresh_token");

                        afterWeChatLogin(accessToken, openId, unionId, expiredIn, refreshToken);
                    }
                });
    }

    private void afterWeChatLogin(final String accessToken, String openId, String unionId, long expiredIn, String refreshToken) {
        HashMap<String, String> map = new HashMap<>(4);
        map.put("type", "wechat");
        map.put("access_token", accessToken);
        map.put("expired", expiredIn + "");
        map.put("union_id", unionId);
        map.put("open_id", openId);
        map.put("refresh_token", refreshToken);
        ApiServer.basePostRequest(this, Constants.LOGIN, map, new TypeToken<ResponseData<LoginData>>(){})
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
            msgIntent.putExtra(LoginActivity.LOGIN_DATA, loginData);
        }
        msgIntent.setAction(identifier);
        this.sendBroadcast(msgIntent);
    }


    @Override
    protected int getLayoutId() {
        return 0;
    }
}
























































