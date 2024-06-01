package top.vchar;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.CharArrayBuffer;
import org.apache.http.util.EntityUtils;
import org.brotli.dec.BrotliInputStream;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.InflaterInputStream;

/**
 * <p>  TODO 功能描述 </p>
 *
 * @author vchar fred
 * @create_date 2024/5/26
 */
public class DouyinApiTest {

    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1";

    @Test
    public void step1() throws Exception {
//        String url = "https://v.douyin.com/ijMmvfNU/";
        String url = "https://v.douyin.com/ijM9Fn8j/";
        HttpGet httpGet = new HttpGet(buildUri(url));
        httpGet.setConfig(setHttpTimeOut());

        httpGet.addHeader("User-Agent", USER_AGENT);
        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate, br");
        httpGet.addHeader("accept-language", "zh-CN,zh;q=0.9");

        HttpClientContext context = HttpClientContext.create();
        try (CloseableHttpClient httpclient = HttpClientBuilder.create()
                .setDefaultCookieStore(new BasicCookieStore())
                .build()) {
            HttpResponse response = httpclient.execute(httpGet, context);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                System.out.println(context.getRedirectLocations());

                if (response.getFirstHeader("Content-Encoding")!=null
                        && "br".equalsIgnoreCase(response.getFirstHeader("Content-Encoding").getValue())) {
                    // 解压Brotli编码的内容
                    byte[] body = decompressBrotli(EntityUtils.toByteArray(response.getEntity()));
                    System.out.println(new String(body));
                } else {
                    // 如果不是Brotli编码，直接打印内容
                    System.out.println(EntityUtils.toString(response.getEntity()));
                }
            } finally {
                EntityUtils.consume(response.getEntity());
            }
        }
    }

    private static byte[] decompressBrotli(byte[] compressed) throws IOException {
        try (BrotliInputStream brotliInputStream = new BrotliInputStream(new ByteArrayInputStream(compressed));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = brotliInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
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
