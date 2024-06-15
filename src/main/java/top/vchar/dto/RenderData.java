package top.vchar.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RenderData {

    @JSONField(name = "_location")
    private String location;

    private RenderDataApp app;

    public List<String> getUrls() {
        if (null == app) {
            return new ArrayList<>();
        }
        return app.getUrls();
    }
}
