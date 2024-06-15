package top.vchar.dto;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import top.vchar.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class VideoInfoRes {

    @JSONField(name = "item_list")
    private List<ItemInfo> itemList;

    public ItemInfo getItemInfo() {
        if (null == itemList || itemList.isEmpty()) {
            return new ItemInfo();
        }
        return itemList.get(0);
    }

    public String getVideoId() {
        JSONObject video = this.getItemInfo().getVideo();
        if (null == video) {
            return null;
        }

        JSONObject playAddr = video.getJSONObject("play_addr");
        if (null == playAddr) {
            return null;
        }

        String videoId = playAddr.getString("uri");
        if (StringUtils.isBlank(videoId)) {
            return null;
        }
        return String.format("https://aweme.snssdk.com/aweme/v1/play/?video_id=%s&ratio=1080p&line=0", videoId);
    }

    public List<String> getImages() {
        List<String> list = new ArrayList<>();
        ItemInfo itemInfo = this.getItemInfo();
        if (null == itemInfo.getImages() || itemInfo.getImages().isEmpty()) {
            return list;
        }

        int size = itemInfo.getImages().size();
        for (int i = 0; i < size; i++) {
            List<String> imgUrls = itemInfo.getImages().getJSONObject(i)
                    .getJSONArray("download_url_list").toJavaList(String.class);
            if (null != imgUrls && !imgUrls.isEmpty()) {
                list.add(imgUrls.get(imgUrls.size() - 1));
            }
        }
        return list;
    }

}
