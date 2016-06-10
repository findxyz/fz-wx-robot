package xyz.fz.wxrobot.po;

import okhttp3.MediaType;

import java.util.Map;

/**
 * Created by fz on 2016/5/30.
 */
public abstract class WxQueryPo {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    protected String url;

    protected String method;

    protected String urlParams;

    protected Map<String, Object> bodyParams;

    public WxQueryPo() {}

    public WxQueryPo(String url, String method, String urlParams, Map<String, Object> bodyParams) {
        this.url = url;
        this.method = method;
        this.urlParams = urlParams;
        this.bodyParams = bodyParams;
    }

    public abstract <T> T result();
}
