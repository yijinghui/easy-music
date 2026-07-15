package com.easy.utils;

import com.easy.config.MeilisearchProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Component
@Slf4j
public class MSUtil {
    @Autowired
    private MeilisearchProperties meilisearchProperties;


    public List<Object> search(String indexName,String keyword,List<String> attributesToHighlight) {
        log.info("搜索关键词: {}", keyword);

        String url = meilisearchProperties.getHostUrl() + "/indexes/" + indexName + "/search";
        Map<String, Object> body = new HashMap<>();
        body.put("q", keyword);
        body.put("attributesToHighlight", attributesToHighlight);
        body.put("attributesToCrop", List.of("lyricsSegment"));
        body.put("cropMarker", "...");


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(meilisearchProperties.getMasterKey());

        // 使用RestTemplate发送发送POST请求
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        Map<String, Object> result = response.getBody();


        List<Map<String, Object>> hits = (List<Map<String, Object>>) result.get("hits");

        // 获取搜索结果
        List<Object> searchResult = new ArrayList<>();
        for (int i = 0; i < hits.size(); i++) {
            Map<String, Object> doc = hits.get(i);
            searchResult.add(doc.get("_formatted"));
        }


        return searchResult;
    }



}
