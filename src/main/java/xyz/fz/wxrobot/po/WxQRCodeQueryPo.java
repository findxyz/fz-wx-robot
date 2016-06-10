package xyz.fz.wxrobot.po;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.fz.wxrobot.util.BaseUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.Map;

import static xyz.fz.wxrobot.WxFun.getOKHttpClientInstance;

/**
 * Created by fz on 2016/5/30.
 */
public class WxQRCodeQueryPo extends WxQueryPo {

    private Logger logger = LoggerFactory.getLogger(WxQRCodeQueryPo.class);

    private String url = "https://login.weixin.qq.com/qrcode/";

    public WxQRCodeQueryPo(String urlParams, Map<String, Object> bodyParams) {
        this.urlParams = urlParams;
        this.bodyParams = bodyParams;
    }

    @Override
    public BufferedImage result() {

        OkHttpClient client = getOKHttpClientInstance();
        RequestBody requestBody = new FormBody.Builder()
                .add("t", bodyParams.get("t").toString())
                .add("_", bodyParams.get("_").toString())
                .build();
        Request request = new Request.Builder().url(url + urlParams).post(requestBody).build();
        BufferedImage bufferedImage = null;
        Response response = null;
        try {
            logger.debug("[POST] " + url);
            response = client.newCall(request).execute();
            bufferedImage = ImageIO.read(response.body().byteStream());
            logger.debug("[POST BACK] " + bufferedImage.toString());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(BaseUtil.getExceptionStackTrace(e));
        } finally {
            if (response != null) {
                response.body().close();
            }
        }
        return bufferedImage;
    }
}
