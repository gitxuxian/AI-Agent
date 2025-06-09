package com.example.imagesearchmcp.tool;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Component
public class ImagesSearchTool {



    @Value("${api.key}")
    private static final String search_api_key = "";

    private static final String search_bash_url = "https://api.pexels.com/v1/search";


    @Tool(description = "seach images from web")
    public String eachPhotos(@ToolParam(description = "Search query keyword") String query) {
        try {
            return String.join(",", searchImages(query));
        } catch (Exception e) {
            return e.getMessage();
        }

    }

    public List<String> searchImages(String query) {
        //设置请求头
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", search_api_key);

        //调用接口
        Map<String, Object> param = new HashMap<>();
        param.put("query", query);

        String response = HttpUtil.
            createGet(search_bash_url)
            .addHeaders(headers)
            .form(param).execute().body();

        return JSONUtil.parseObj(response)
            .getJSONArray("photos")
            .stream()
            .map(photoObj -> (JSONObject) photoObj)
            .map(photoObj -> photoObj.getJSONObject("src"))
            .map(photo -> photo.getStr("medium"))
            .filter(StrUtil::isNotBlank)
            .collect(Collectors.toList());

    }

}
