package xyz.fz.wxrobot;

import okhttp3.OkHttpClient;
import org.apache.commons.lang.StringUtils;
import xyz.fz.wxrobot.cookiejar.WxLoginCookieJar;
import xyz.fz.wxrobot.po.*;
import xyz.fz.wxrobot.util.BaseUtil;

import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * Created by fz on 2016/5/29.
 */
public class WxFun {

    private static OkHttpClient client = new OkHttpClient.Builder().cookieJar(new WxLoginCookieJar()).build();

    public static OkHttpClient getOKHttpClientInstance() {
        return client;
    }

    static {
        System.setProperty("jsse.enableSNIExtension", "false");
    }

    public static Map<String, Object> queryUUID() {

        String urlParams = "";
        try {
            urlParams += "?appid=wx782c26e4c19acffb";
            urlParams += "&fun=new";
            urlParams += "&lang=zh_CN";
            urlParams += "&_=" + System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
        }
        WxUUIDQueryPo wxUUIDQueryPo = new WxUUIDQueryPo(urlParams);
        return wxUUIDQueryPo.result();
    }

    public static BufferedImage queryQRCode(String uuid) {

        String urlParams = uuid;
        Map<String, Object> bodyParams = new HashMap<>();
        bodyParams.put("t", "webwx");
        bodyParams.put("_", System.currentTimeMillis());
        WxQRCodeQueryPo wxQRCodeQueryPo = new WxQRCodeQueryPo(urlParams, bodyParams);
        return wxQRCodeQueryPo.result();
    }

    public static Map<String, Object> checkConfirm(String uuid) {

        String urlParams = "?tip=1&uuid=" + uuid + "&_=" + System.currentTimeMillis();
        WxCheckConfirmQueryPo checkConfirmQueryPo = new WxCheckConfirmQueryPo(urlParams);
        return checkConfirmQueryPo.result();
    }

    public static Map<String, Object> getCookie(String url) {

        String urlParams = "&fun=new";
        WxGetCookieQueryPo wxGetCookieQueryPo = new WxGetCookieQueryPo(url, urlParams);
        return wxGetCookieQueryPo.result();
    }

    public static Map<String, Object> init(Map<String, Object> baseRequest) {

        WxInitQueryPo wxInitQueryPo = new WxInitQueryPo(baseRequest);
        return wxInitQueryPo.result();
    }

    public static Vector getContactList(Map<String, Object> baseRequest) {

        WxContactListQueryPo wxContactListQueryPo = new WxContactListQueryPo(baseRequest);
        return wxContactListQueryPo.result();
    }

    public static Map<String, Object> syncNewMsg(Map<String, Object> baseRequest, String pass_ticket, Map syncKey) {

        Map<String, Object> BaseRequest = (Map<String, Object>) baseRequest.get("BaseRequest");
        String urlParams = "?sid=" + BaseRequest.get("Sid") + "&skey=" + BaseRequest.get("Skey") + "&pass_ticket=" + pass_ticket + "&r=" + System.currentTimeMillis();
        baseRequest.put("SyncKey", syncKey);
        String nowTime = System.currentTimeMillis() + "";
        baseRequest.put("rr", nowTime);
        WxSyncNewMsgQueryPo wxSyncNewMsgQueryPo = new WxSyncNewMsgQueryPo(urlParams, baseRequest);
        return wxSyncNewMsgQueryPo.result();
    }

    public static String checkNewMsg(Map<String, Object> baseRequest, Map syncKey) {

        String strSyncKey = "";
        StringBuilder sb = new StringBuilder();
        List list = (List) syncKey.get("List");
        for(int i=0; i<list.size(); i++){
            Map data = (Map) list.get(i);
            sb.append("|" + data.get("Key") + "_" + data.get("Val"));
        }
        if (StringUtils.isNotBlank(sb.toString())) {
            strSyncKey = sb.toString().substring(1);
        }
        Map BaseRequest = (Map) baseRequest.get("BaseRequest");
        String urlParams = "?r=" + System.currentTimeMillis() + BaseUtil.getRandomNumber(5);
        try {
            urlParams += "&skey=" + BaseRequest.get("Skey").toString();
            urlParams += "&uin=" + BaseRequest.get("Uin").toString();
            urlParams += "&sid=" + URLEncoder.encode(BaseRequest.get("Sid").toString(), "utf-8");
            urlParams += "&deviceId=" + URLEncoder.encode(BaseRequest.get("DeviceId").toString(), "utf-8");
            urlParams += "&synckey=" + URLEncoder.encode(strSyncKey, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        urlParams += "&_=" + System.currentTimeMillis();
        WxCheckNewMsgQueryPo wxCheckNewMsgQueryPo = new WxCheckNewMsgQueryPo(urlParams, baseRequest);
        return wxCheckNewMsgQueryPo.result();
    }

    public static boolean sendMsg(String fromUserId, String toUserId, String msg, Map<String, Object> baseRequest, String pass_ticket) {

        String urlParams = "?pass_ticket=" + pass_ticket;
        Map msgMap = new HashMap<>();
        msgMap.put("Type", 1);
        msgMap.put("Content", msg);
        msgMap.put("FromUserName", fromUserId);
        msgMap.put("ToUserName", toUserId);
        String now = System.currentTimeMillis() + BaseUtil.getRandomNumber(5);
        msgMap.put("LocalID", now);
        msgMap.put("ClientMsgId", now);
        baseRequest.put("Msg", msgMap);
        WxSendMsgQueryPo wxSendMsgQueryPo = new WxSendMsgQueryPo(urlParams, baseRequest);
        return wxSendMsgQueryPo.result();
    }
}
