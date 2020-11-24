package top.vchar.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import top.vchar.AppUI;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * <p> 请求帮助类 </p>
 *
 * @author vchar fred
 * @version 1.0
 * @create_date 2020/11/23
 */
public class TikTokHttpUtil {

    /**
     * 请求超时时间 10s
     */
    public static final int CONNECT_TIMEOUT = 10000;
    public static final int CONNECTION_REQUEST_TIMEOUT = 10000;
    public static final int SOCKET_TIMEOUT = 10000;
    private static final String USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1";


    private CloseableHttpClient httpClient = null;

    private AppUI appUI = null;

    private TikTokHttpUtil(AppUI appUI) {
        CookieStore cookieStore = new BasicCookieStore();
        this.httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();
        this.appUI = appUI;
    }

    private static TikTokHttpUtil tikTokHttpUtil;

    public static TikTokHttpUtil getInstance(AppUI appUI) {
        if (tikTokHttpUtil == null) {
            synchronized (TikTokHttpUtil.class) {
                if (tikTokHttpUtil == null) {
                    tikTokHttpUtil = new TikTokHttpUtil(appUI);
                }
            }
        }
        return tikTokHttpUtil;
    }

    /**
     * 提取抖音无水印视频链接
     *
     * @param tikTokVideoShareUrl 抖音分享视频连接
     * @return 返回抖音无水印视频链接
     */
    public String extractVideoUrl(String tikTokVideoShareUrl) throws Exception {
        //过滤链接，获取http连接地址
        String url = decodeHttpUrl(tikTokVideoShareUrl);
        if (appUI != null) {
            appUI.updateUrl(url);
        }

        url = execute(url);

        String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
        JSONObject data = JSONObject.parseObject(body).getJSONArray("item_list").getJSONObject(0);
        printLog(data.getString("desc"));
        String videoId = data.getJSONObject("video")
                .getJSONObject("play_addr").getString("uri");
        return String.format("https://aweme.snssdk.com/aweme/v1/play/?video_id=%s&ratio=720p&line=0", videoId);
    }

    public String execute(String url) throws Exception {
        HttpGet httpGet = new HttpGet(buildUri(url));
        httpGet.setConfig(setHttpTimeOut());

        httpGet.addHeader("User-Agent", USER_AGENT);
        httpGet.addHeader("Accept", "*/*");
        httpGet.addHeader("Accept-Encoding", "gzip, deflate, br");
        httpGet.addHeader("accept-language", "zh-CN,zh;q=0.9");

        HttpClientContext context = HttpClientContext.create();
        try (CloseableHttpResponse response = httpClient.execute(httpGet, context)) {
            URI uri = context.getRedirectLocations().get(0);
            url = uri.toString();
            String[] arr = url.split("\\?")[0].split("/");
            return "https://www.iesdouyin.com/web/api/v2/aweme/iteminfo/?item_ids=" + arr[arr.length - 1];
        } catch (IOException e) {
            printLog("提取视频下载地址异常!");
            throw new NullPointerException("提取视频下载地址异常!");
        }
    }

    public String decodeHttpUrl(String url) {
        try {
            int start = url.indexOf("http");
            int end = url.lastIndexOf("/");
            return url.substring(start, end + 1);
        } catch (Exception e) {
            printLog("解析抖音视频分享链接失败.");
            throw new NullPointerException("提取视频下载地址异常!");
        }

    }

    /**
     * 设置请求超时
     *
     * @return 返回新的请求配置
     */
    private static RequestConfig setHttpTimeOut() {
        return RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .build();
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
            printLog(String.format("请求地址[%s]不正确", url));
            throw new Exception(e);
        }
    }

    public void downloadVideo(String videoUrl, String dir, String fileName) {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Connection", "keep-alive");
        headers.put("Host", "aweme.snssdk.com");
        headers.put("User-Agent", USER_AGENT);

        OutputStream out = null;
        BufferedInputStream in = null;
        try {
            in = Jsoup.connect(videoUrl).headers(headers).referrer("https://www.iesdouyin.com/").timeout(20000).ignoreContentType(true).execute().bodyStream();
            if (null == fileName) {
                fileName = System.currentTimeMillis() + ".mp4";
            }
            File fileSavePath = new File(dir + fileName);
            File fileParent = fileSavePath.getParentFile();
            if (!fileParent.exists()) {
                if (!fileParent.mkdirs()) {
                    printLog("文件创建失败，请检查文件路径是否正确");
                    return;
                }
            }

            out = new BufferedOutputStream(new FileOutputStream(fileSavePath));
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }

            printLog("视频下载成功");
            printLog("-----抖音去水印链接-----\n" + videoUrl);
            printLog("-----视频保存路径-----\n" + fileSavePath.getAbsolutePath());
        } catch (IOException e) {
            printLog("视频下载失败: " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    //关闭输出流
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (in != null) {
                try {
                    //关闭输入流
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void printLog(String message) {
        if (this.appUI != null) {
            appUI.addLog(message);
        } else {
            System.out.println(message);
        }
    }

}
