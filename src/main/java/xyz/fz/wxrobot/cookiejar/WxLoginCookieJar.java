package xyz.fz.wxrobot.cookiejar;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fz on 2016/5/30.
 */
public class WxLoginCookieJar implements CookieJar {

    // 只保存此地址返回的cookie，用作后续请求的cookie
    private String loginUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxnewloginpage";

    private List<Cookie> cookies;

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (url.url().toString().contains(loginUrl)) {
            this.cookies = cookies;
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        return cookies != null ? cookies : new ArrayList<>();
    }
}
