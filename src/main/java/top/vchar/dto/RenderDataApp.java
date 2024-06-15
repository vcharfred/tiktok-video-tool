package top.vchar.dto;

import lombok.Data;
import top.vchar.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>  TODO 功能描述 </p>
 *
 * @author vchar fred
 * @create_date 2024/6/10
 */
@Data
public class RenderDataApp {

    private VideoInfoRes videoInfoRes;

    public List<String> getUrls() {
        List<String> urls = new ArrayList<>();
        if (null == videoInfoRes) {
            return urls;
        }
        urls = this.videoInfoRes.getImages();
        if (urls.isEmpty()) {
            String videoId = this.getVideoInfoRes().getVideoId();
            if (!StringUtils.isBlank(videoId)) {
                urls.add(videoId);
            }
        }
        return urls;
    }

}
