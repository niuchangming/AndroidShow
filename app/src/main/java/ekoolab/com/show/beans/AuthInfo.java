package ekoolab.com.show.beans;

import org.json.JSONObject;

import java.util.Date;

public class AuthInfo {

    String mobile;
    String dialNo;
    String apiToken;
    int role;
    String accountType;
    String nickName;
    String userCode;

    String fbAccessToken;
    String fbUserId;
    Date fbExpiredDate;

    String wxAccessToken;
    String wxUnionId;
    Date wxExpiredDate;

    public AuthInfo (JSONObject jsonData){
        apiToken = jsonData.optString("token");
        role = jsonData.optInt("roleType");
        accountType = jsonData.optString("accountType");
        nickName = jsonData.optString("nickName");
        userCode = jsonData.optString("userCode");
    }

}
