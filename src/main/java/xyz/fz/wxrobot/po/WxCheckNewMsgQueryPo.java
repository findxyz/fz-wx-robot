package xyz.fz.wxrobot.po;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.wxrobot.util.BaseUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static xyz.fz.wxrobot.WxFun.getOKHttpClientInstance;

/**
 * Created by fz on 2016/5/30.
 */
public class WxCheckNewMsgQueryPo extends WxQueryPo {

    private Logger logger = LoggerFactory.getLogger(WxCheckNewMsgQueryPo.class);

    private static Pattern selectorPattern = Pattern.compile("selector:\"(\\S+)\"");

    private String url = "https://webpush.weixin.qq.com/cgi-bin/mmwebwx-bin/synccheck";

    public WxCheckNewMsgQueryPo(String urlParams, Map<String, Object> bodyParams) {
        this.urlParams = urlParams;
        this.bodyParams = bodyParams;
    }

    @Override
    public String result() {

        String result = "0";
        OkHttpClient client = getOKHttpClientInstance();
        Request request = new Request.Builder().url(url + urlParams).build();
        String responseBody = "";
        try {
            logger.debug("[GET] " + url + urlParams);
            Response response = client.newCall(request).execute();
            responseBody = response.body().string();
            Matcher matcher = selectorPattern.matcher(responseBody);
            matcher.find();
            result = matcher.group(1);
            logger.debug("[GET BACK] " + responseBody);
        } catch (Exception e) {
            logger.warn(BaseUtil.getExceptionStackTrace(e));
            logger.debug(responseBody);
        }
        return result;
    }
}
