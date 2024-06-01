package top.vchar;

import org.junit.Test;
import top.vchar.util.TikTokHttpUtil;

/**
 * <p>  TODO 功能描述 </p>
 *
 * @author vchar fred
 * @create_date 2024/5/31
 */
public class TikTokHttpUtilTest {

    @Test
    public void downVide(){
        TikTokHttpUtil tikTokHttpUtil = TikTokHttpUtil.getInstance(new AppUI());
        String url = "https://aweme.snssdk.com/aweme/v1/play/?video_id=v0200fg10000cobji1rc77u6qgvg67dg&ratio=1080P&line=0";

        tikTokHttpUtil.downloadVideo(url, "./", "demo.mp4");
    }
}
