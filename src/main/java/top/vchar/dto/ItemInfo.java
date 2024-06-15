package top.vchar.dto;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class ItemInfo {

    private JSONObject video;

    @JSONField(name = "img_bitrate")
    private JSONArray images;

}
