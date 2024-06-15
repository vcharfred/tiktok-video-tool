package top.vchar.dto;

import lombok.Data;
import top.vchar.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
