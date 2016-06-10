package xyz.fz.wxrobot.po;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.wxrobot.util.BaseUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xyz.fz.wxrobot.WxFun.getOKHttpClientInstance;

/**
 * Created by fz on 2016/5/30.
 */
public class WxUUIDQueryPo extends WxQueryPo {

    private Logger logger = LoggerFactory.getLogger(WxUUIDQueryPo.class);

    private static Pattern uuidPattern = Pattern.compile("window.QRLogin.code = (\\d+); window.QRLogin.uuid = \"(\\S+)\";");

    private String url = "https://login.weixin.qq.com/jslogin";

    public WxUUIDQueryPo(String urlParams) {
        this.urlParams = urlParams;
    }

    @Override
    public Map<String, Object> result() {

        Map<String, Object> result = new HashMap<>();
        OkHttpClient client = getOKHttpClientInstance();
        Request request = new Request.Builder().url(url + urlParams).build();
        String responseBody = "";
        try {
            logger.debug("[GET] " + url + urlParams);
            Response response = client.newCall(request).execute();
            responseBody = response.body().string();
            logger.debug("[GET BACK] " + responseBody);
            Matcher matcher = uuidPattern.matcher(responseBody);
            matcher.matches();
            result.put("code", matcher.group(1));
            result.put("uuid", matcher.group(2));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
            result.put("error", responseBody);
        }
        return result;
    }
}
