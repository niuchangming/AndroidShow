package ekoolab.com.show.beans;

import org.json.JSONObject;

import java.util.Date;

public class AuthInfo {

    public String mobile;
    public String dialNo;
    public String apiToken;
    public int role;
    public String accountType;
    public String nickName;
    public String userCode;

    public String fbAccessToken;
    public String fbUserId;
    public Date fbExpiredDate;

    public String wxAccessToken;
    public String wxUnionId;
    public Date wxExpiredDate;

    public AuthInfo (JSONObject jsonData){
        apiToken = jsonData.optString("token");
        role = jsonData.optInt("roleType");
        accountType = jsonData.optString("accountType");
        nickName = jsonData.optString("nickName");
        userCode = jsonData.optString("userCode");
    }

}
