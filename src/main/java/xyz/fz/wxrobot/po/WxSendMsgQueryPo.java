package xyz.fz.wxrobot.po;

import com.fasterxml.jackson.core.JsonProcessingException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.wxrobot.util.BaseUtil;

import java.util.HashMap;
import java.util.Map;

import static xyz.fz.wxrobot.WxFun.getOKHttpClientInstance;

/**
 * Created by fz on 2016/5/30.
 */
public class WxSendMsgQueryPo extends WxQueryPo {

    private Logger logger = LoggerFactory.getLogger(WxSendMsgQueryPo.class);

    private String url = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsg";

    public WxSendMsgQueryPo(String urlParams, Map<String, Object> bodyParams) {
        this.urlParams = urlParams;
        this.bodyParams = bodyParams;
    }

    @Override
    public Boolean result() {

        Map<String, Object> result = new HashMap<>();
        OkHttpClient client = getOKHttpClientInstance();
        RequestBody body = null;
        try {
            body = RequestBody.create(JSON, BaseUtil.toJson(bodyParams));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
        }
        Request request = new Request.Builder().url(url + urlParams).post(body).build();
        String responseBody = "";
        boolean sendMsgFlag = false;
        try {
            logger.debug("[POST] " + url + urlParams);
            Response response = client.newCall(request).execute();
            responseBody = response.body().string();
            result = BaseUtil.parseJson(responseBody, Map.class);
            Map BaseResponse = (Map) result.get("BaseResponse");
            if (BaseResponse.get("Ret").toString().equals("0")) {
                sendMsgFlag = true;
            }
            logger.debug("[POST BACK] " + responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
            result.put("error", responseBody);
        }
        return sendMsgFlag;
    }
}
