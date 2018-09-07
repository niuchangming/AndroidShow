package ekoolab.com.show.beans;

import org.json.JSONObject;

import java.util.Date;

public class AuthInfo {

    private String mobile;
    private String dialNo;
    private String apiToken;
    private int role;
    private String accountType;
    private String nickName;
    private String userCode;

    private String fbAccessToken;
    private String fbUserId;
    private Date fbExpiredDate;

    private String wxAccessToken;
    private String wxUnionId;
    private Date wxExpiredDate;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDialNo() {
        return dialNo;
    }

    public void setDialNo(String dialNo) {
        this.dialNo = dialNo;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getUserCode() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode = userCode;
    }

    public String getFbAccessToken() {
        return fbAccessToken;
    }

    public void setFbAccessToken(String fbAccessToken) {
        this.fbAccessToken = fbAccessToken;
    }

    public String getFbUserId() {
        return fbUserId;
    }

    public void setFbUserId(String fbUserId) {
        this.fbUserId = fbUserId;
    }

    public Date getFbExpiredDate() {
        return fbExpiredDate;
    }

    public void setFbExpiredDate(Date fbExpiredDate) {
        this.fbExpiredDate = fbExpiredDate;
    }

    public String getWxAccessToken() {
        return wxAccessToken;
    }

    public void setWxAccessToken(String wxAccessToken) {
        this.wxAccessToken = wxAccessToken;
    }

    public String getWxUnionId() {
        return wxUnionId;
    }

    public void setWxUnionId(String wxUnionId) {
        this.wxUnionId = wxUnionId;
    }

    public Date getWxExpiredDate() {
        return wxExpiredDate;
    }

    public void setWxExpiredDate(Date wxExpiredDate) {
        this.wxExpiredDate = wxExpiredDate;
    }

    public AuthInfo (JSONObject jsonData){
        apiToken = jsonData.optString("token");
        role = jsonData.optInt("roleType");
        accountType = jsonData.optString("accountType");
        nickName = jsonData.optString("nickName");
        userCode = jsonData.optString("userCode");
    }

}
