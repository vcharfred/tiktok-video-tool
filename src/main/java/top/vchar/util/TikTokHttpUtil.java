package top.vchar.util;

import com.alibaba.fastjson.JSONArray;
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
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    public List<String> extractVideoUrl(String tikTokVideoShareUrl) throws Exception {
        //过滤链接，获取http连接地址
        String url = decodeHttpUrl(tikTokVideoShareUrl);
        if (appUI != null) {
            appUI.updateUrl(url);
        }

        url = execute(url);

        String body = Jsoup.connect(url).ignoreContentType(true).execute().body();
        JSONObject data = JSONObject.parseObject(body).getJSONArray("item_list").getJSONObject(0);
        printLog(data.getString("desc"));

        List<String> urlList = new ArrayList<>();
        JSONObject videoJson = data.getJSONObject("video");
        String videoId = videoJson.getString("vid");
        if(StringUtils.isNotBlank(videoId)){
            urlList.add(String.format("https://aweme.snssdk.com/aweme/v1/play/?video_id=%s&ratio=1080P&line=0", videoId));
        }else {
            JSONArray images = data.getJSONArray("images");
            for(int i=0; i<images.size(); i++){
                JSONObject imageJson = images.getJSONObject(i);
                JSONArray array = imageJson.getJSONArray("url_list");
                if(null!=array&&array.size()>0){
                    String imageUrl = array.getString(0);
                    for(int j=0; j<array.size(); j++){
                        String imageUrl1 = array.getString(j);
                        if(imageUrl1.contains(".jpeg")){
                            imageUrl = imageUrl1;
                            break;
                        }
                    }
                    urlList.add(imageUrl);
                }
            }
        }
        return urlList;
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
            String dyUri = url.substring(start);
            int end = dyUri.indexOf("/ ");
            if (end<1) {
                end = dyUri.lastIndexOf("/");
            }
            return dyUri.substring(0, end + 1);
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
        printLog("抖音去水印链接:" + videoUrl);

        Map<String, String> headers = new HashMap<>(3);
        headers.put("Connection", "keep-alive");
        headers.put("Host", "aweme.snssdk.com");
        headers.put("User-Agent", USER_AGENT);

        OutputStream out = null;
        BufferedInputStream in = null;
        try {
            in = Jsoup.connect(videoUrl).headers(headers).referrer("https://www.iesdouyin.com/").timeout(20000).ignoreContentType(true).execute().bodyStream();
            out = buildFileOutOutStream(dir, fileName);
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
            printLog("视频下载成功");
        } catch (IOException e) {
            printLog("视频下载失败: " + e.getMessage());
            // 由于jsoup偶尔会出现失败的情况，这里切换到httpclient
            retryDownloadVideo(videoUrl, dir, fileName);
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

    private void retryDownloadVideo(String videoUrl, String dir, String fileName) {
        printLog("开始尝试通过其他方式下载视频，若依然失败请手动下载");
        try{
            HttpGet httpGet = new HttpGet(buildUri(videoUrl));
            httpGet.setConfig(setHttpTimeOut());

            httpGet.addHeader("User-Agent", "PostmanRuntime/7.29.2");
            httpGet.addHeader("Accept", "*/*");
            httpGet.addHeader("Host", "aweme.snssdk.com");
            httpGet.addHeader("Accept-Encoding", "gzip, deflate, br");
            httpGet.addHeader("Connection", "keep-alive");
            HttpClientContext context = HttpClientContext.create();
            try (CloseableHttpResponse req = httpClient.execute(httpGet, context)) {
                URI uri = context.getRedirectLocations().get(0);
                printLog("抖音去水印链接:" + uri.toString());
                HttpGet httpGet2 = new HttpGet(uri);
                httpGet2.setConfig(setHttpTimeOut());
                httpGet2.addHeader("User-Agent", USER_AGENT);
                httpGet2.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                httpGet2.addHeader("Accept-Encoding", "gzip, deflate, br");
                httpGet2.addHeader("Connection", "keep-alive");
                HttpClientContext context2 = HttpClientContext.create();
                try (CloseableHttpResponse response = httpClient.execute(httpGet2, context2)) {
                    if(response.getStatusLine().getStatusCode()==200){
                        InputStream in = response.getEntity().getContent();
                        try (OutputStream out = buildFileOutOutStream(dir, fileName)){
                            int b;
                            while ((b = in.read()) != -1) {
                                out.write(b);
                            }
                            printLog("视频下载成功");
                        }
                    }else {
                        printLog("视频下载失败："+response.getStatusLine());
                    }
                }
            } catch (IOException e) {
                printLog("尝试重新下载失败: "+e.getMessage());
            }
        }catch (Exception e){
            printLog("下载地址解析失败!");
        }
    }

    public void downloadImage(String imageUrl, String dir){
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Connection", "keep-alive");
        headers.put("Host", "aweme.snssdk.com");
        headers.put("User-Agent", USER_AGENT);
        OutputStream out = null;
        BufferedInputStream in = null;
        try {
            in = Jsoup.connect(imageUrl).headers(headers).timeout(20000)
                    .ignoreContentType(true)
                    .execute()
                    .bodyStream();
            String fileName = System.currentTimeMillis() + ".jpeg";
            File fileSavePath = new File(dir + fileName);
            File fileParent = fileSavePath.getParentFile();
            if (!fileParent.exists()) {
                if (!fileParent.mkdirs()) {
                    printLog("文件创建失败，请检查文件路径是否正确");
                    return;
                }
            }

            out = new BufferedOutputStream(Files.newOutputStream(fileSavePath.toPath()));
            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }

            printLog("图片下载成功:" + imageUrl);
            printLog("图片保存路径:" + fileSavePath.getAbsolutePath()+"\n");
        } catch (IOException e) {
            printLog("图片下载失败: " + e.getMessage());
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

    private OutputStream buildFileOutOutStream(String dir, String fileName) throws IOException {
        if (null == fileName) {
            fileName = System.currentTimeMillis() + ".mp4";
        }
        File fileSavePath = new File(dir + fileName);
        File fileParent = fileSavePath.getParentFile();
        if (!fileParent.exists()) {
            if (!fileParent.mkdirs()) {
                printLog("文件创建失败，请检查文件路径是否正确");
                throw new IOException("文件创建失败，请检查文件路径是否正确");
            }
        }
        printLog("视频保存路径:" + fileSavePath.getAbsolutePath());
        return new BufferedOutputStream(Files.newOutputStream(fileSavePath.toPath()));
    }
}
