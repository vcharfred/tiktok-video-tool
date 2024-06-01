package top.vchar;

import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * <p>  TODO 功能描述 </p>
 *
 * @author vchar fred
 * @create_date 2024/6/1
 */
public class DyApiTest {

    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1";


    @Test
    public void apiTest() throws Exception {
//        https://www.iesdouyin.com/share/video/7368454503525256458/
        String url = "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=7368454503525256458&msToken=OD0opfEU9q8jjqJmJAV681QG9ejTJqIKkEZXPZ9m3NbQEyeRGqtX1GzmuTuC6DsQ5u7L4CzpF30X49BvCs6T1kKMbcizdwa59ITVWkqUFiV2zuY9CuiW&a_bogus=YflOvOhsMsm1uDVbSwkz9asm2u80YW-EgZEz2SFIAzqh";
        HttpGet httpGet = new HttpGet(buildUri(url));
        httpGet.setConfig(setHttpTimeOut());

        httpGet.addHeader("User-Agent", USER_AGENT);
        httpGet.addHeader("Host", "www.iesdouyin.com");
        httpGet.addHeader("referer", "https://www.iesdouyin.com");
        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("agw-js-conv", "str");
        httpGet.addHeader("accept-language", "zh-CN,zh;q=0.9");
        httpGet.addHeader("Cookie", "ttwid=1%7Cq_EGjuKHSxFWgyayAO51xzx6A7-Gi0CMbmKPzmuRBnk%7C1717206219%7Cdb83a306964fa87e4da5fc4ffa99ae2631b4f5480b4bd7a2948f2408c25079ae; msToken=RbzqgABdqdkYWhdSnWOW88W_fAUfGsjrNDksXgi0YNWVUSijvwSTE_87D76xLhesaPveRFy1ewAaDm5zQwkS_LZnczTfWWKm-ZF18KgCzwBIES5eBFWO");

        HttpClientContext context = HttpClientContext.create();
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCookieStore(new BasicCookieStore())
                .build()) {
            HttpResponse response = httpclient.execute(httpGet, context);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println(context.getRedirectLocations());
                // 如果不是Brotli编码，直接打印内容
                System.out.println(EntityUtils.toString(response.getEntity()));
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    /**
     * 构建URI
     *
     * @param url 请求地址
     * @return 返回结果
     */
    private URI buildUri(String url) throws Exception {
        try {
            URIBuilder uriBuilder = new URIBuilder(url);
            return uriBuilder.build();
        } catch (URISyntaxException e) {
            throw new Exception(e);
        }
    }

    /**
     * 设置请求超时
     *
     * @return 返回新请求配置
     */
    private static RequestConfig setHttpTimeOut() {
        return RequestConfig.custom()
                .setConnectTimeout(10000)
                .setConnectionRequestTimeout(10000)
                .setSocketTimeout(10000)
                .setRedirectsEnabled(true)
                .build();
    }
}
