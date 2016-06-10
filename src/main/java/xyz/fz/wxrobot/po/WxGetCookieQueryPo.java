package xyz.fz.wxrobot.po;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.wxrobot.util.BaseUtil;

import java.util.HashMap;
import java.util.Map;

import static xyz.fz.wxrobot.WxFun.getOKHttpClientInstance;

/**
 * Created by fz on 2016/5/30.
 */
public class WxGetCookieQueryPo extends WxQueryPo {

    private Logger logger = LoggerFactory.getLogger(WxGetCookieQueryPo.class);

    public WxGetCookieQueryPo(String url, String urlParams) {
        this.url = url;
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
            Document document = DocumentHelper.parseText(responseBody);
            Element root = document.getRootElement();
            Element ret = root.element("ret");
            Element message = root.element("message");
            Element skey = root.element("skey");
            Element wxsid = root.element("wxsid");
            Element wxuin = root.element("wxuin");
            Element pass_ticket = root.element("pass_ticket");
            logger.debug("ret: " + ret.getText());
            logger.debug("message: " + message.getText());
            logger.debug("skey: " + skey.getText());
            logger.debug("wxsid: " + wxsid.getText());
            logger.debug("wxuin: " + wxuin.getText());
            logger.debug("pass_ticket: " + pass_ticket.getText());
            result.put("ret", ret.getText());
            result.put("message", message.getText());
            result.put("skey", skey.getText());
            result.put("wxsid", wxsid.getText());
            result.put("wxuin", wxuin.getText());
            result.put("pass_ticket", pass_ticket.getText());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
            result.put("error", responseBody);
        }
        return result;
    }
}
