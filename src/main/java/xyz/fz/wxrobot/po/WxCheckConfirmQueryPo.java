package xyz.fz.wxrobot.po;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
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
public class WxCheckConfirmQueryPo extends WxQueryPo {

    private Logger logger = LoggerFactory.getLogger(WxCheckConfirmQueryPo.class);

    private static Pattern codePattern = Pattern.compile("window.code=(\\d+);");
    private static Pattern redirectPattern = Pattern.compile("window.redirect_uri=\"(\\S+)\";");

    private String url = "https://login.weixin.qq.com/cgi-bin/mmwebwx-bin/login";

    public WxCheckConfirmQueryPo(String urlParams) {
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
            Matcher matcher = codePattern.matcher(responseBody);
            matcher.find();
            String code = matcher.group(1);
            result.put("code", code);
            if (StringUtils.equals(code, "200")) {
                Matcher matcher2 = redirectPattern.matcher(responseBody);
                matcher2.find();
                result.put("redirect", matcher2.group(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
            result.put("error", responseBody);
        }
        return result;
    }
}
