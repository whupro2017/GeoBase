package whu.lbs;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.httpclient.methods.PostMethod;

public class TencentFlow {
    public String action() {
        String jsonIOFlow = "";
        PostMethod post = new PostMethod("https://heat.qq.com/interface/flow");
        TencentFlowParam tfp = new TencentFlowParam();
        String parm = tfp.toJSONString();
        return action();
    }
}
