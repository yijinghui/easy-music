package com.easy.test;


import cn.hutool.core.bean.BeanUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.json.JsonData;
import com.baomidou.mybatisplus.annotation.TableField;
import com.easy.mapper.PlaylistMapper;
import com.easy.mapper.SongMapper;
import com.easy.pojo.entity.Playlist;
import com.easy.pojo.entity.Song;
import com.easy.service.PlaylistService;
import com.easy.service.SongService;
import com.easy.service.impl.SongServiceImpl;
import com.easy.utils.EsClientUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ESTest {

    @Autowired
    private SongMapper songMapper;
    @Autowired
    private PlaylistMapper playlistMapper;

    @Test
    public void testCreateIndex() throws IOException {
        ElasticsearchClient esClient = EsClientUtil.getEsClient();
        esClient.indices().create(c -> c.index("test_index"));

    }


    @Test
    public void testCreateSuccessIndex() throws IOException {
        ElasticsearchClient client = EsClientUtil.getEsClient();
        ElasticsearchIndicesClient indexClient = client.indices();

        boolean flag = indexClient.exists(req -> req.index("test_index")).value();
        //CreateIndexResponse createIndexResponse = null;
        boolean result = false;
        if (flag) {
            // 目标索引已存在
            log.info("索引[" + "test_index" + "]已存在！");
        } else {
            // 不存在
            result = indexClient.create(req -> req.index("test_index")).acknowledged();
            if (result) {
                log.info("索引[" + "test_index" + "]创建成功！");
            } else {
                log.info("索引[" + "test_index" + "]创建失败！");
            }
        }
    }

    @Test
    public void testAddDocument() throws IOException {
        Song song = songMapper.selectById(548);
        ElasticsearchClient client = EsClientUtil.getEsClient();
        IndexResponse response = client.index(i -> i
                .index("music")
                .id(song.getSongId().toString())
                .document(song)
        );

        log.info("Indexed with version{} " , response.version());

    }

    /*PUT /playlist
    {
        "mappings": {
        "properties": {
            "playlistId": {
                "type": "keyword"
            },
            "userId": {
                "type": "keyword"
            },
            "title": {
                "type": "text",
                        "analyzer": "ik_smart",
                        "copy_to": "all"
            },
            "username": {
                "type":"keyword"
            },
            "style":{
                "type": "text",
                        "analyzer": "ik_smart"
            },
            "coverUrl":{
                "type": "keyword"
            },
            "introduction":{
                "type": "keyword"
            },
            "createTime":{
                "type": "keyword"
            },
            "updateTime":{
                "type": "keyword"
            }
        }
    }*/
    @Test
    public void testBulkPlaylistDocument() throws IOException {
        ElasticsearchClient client = EsClientUtil.getEsClient();
        List<Playlist> playlists = playlistMapper.selectList(null);

        BulkRequest.Builder builder = new BulkRequest.Builder();

        for (Playlist sd : playlists) {
            // 只取需要的字段，构建一个新的 Map
            Map<String, Object> doc = new HashMap<>();
            doc.put("playlistId", sd.getPlaylistId());
            doc.put("title", sd.getTitle());
            doc.put("style", sd.getStyle());
            doc.put("coverUrl", sd.getCoverUrl());

            builder.operations(op -> op
                    .index(idx -> idx
                            .index("playlist")
                            .id(sd.getPlaylistId().toString())
                            .document(doc)  // 只传这 8 个字段
                    )
            );
        }

        BulkRequest bulkRequest = builder.build();
        BulkResponse responses = client.bulk(bulkRequest);

        if (responses.errors()) {
            for (BulkResponseItem item : responses.items()) {
                if (item.error() != null) {
                    System.err.println("写入失败: " + item.error().reason());
                }
            }
        }
    }

    @Test
    public void testBulkDocument() throws IOException {
        ElasticsearchClient client = EsClientUtil.getEsClient();
        List<Song> songs = songMapper.selectList(null);

        BulkRequest.Builder builder = new BulkRequest.Builder();
        // 3. 添加多个操作到批量请求中
        for (Song sd : songs) {
            builder.operations(op -> op
                    .index(idx -> idx
                            .index("music")
                            .id(sd.getSongId().toString())
                            .document(sd)
                    )
            );
        }

        BulkRequest bulkRequest = builder.build();
        BulkResponse responses = client.bulk(bulkRequest);
    }

    @Test
    public void testMatchAll() throws IOException {
        ElasticsearchClient client = EsClientUtil.getEsClient();

        SearchResponse<Song> response = client.search(s -> s
                        .index("music")
                        .query(q -> q
                                .multiMatch(m -> m
                                        .fields("songName", "style","artistName", "album", "lyrics")
                                        .query("故事的小黄花")
                                        .type(TextQueryType.Phrase)
                                )
                        )
                        .highlight(h -> h
                                .fields("songName", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                                .fields("artistName", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                                .fields("album", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                                .fields("lyrics", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                        .numberOfFragments(1)
                                        .fragmentSize(20)
                                )
                                .fields("style", f -> f
                                        .preTags("<em>")
                                        .postTags("</em>")
                                )
                        )
                        .size(10),  // 返回前10条
                Song.class
        );

        // 高亮处理
        if (response!=null) {
            long total = response.hits().total().value();
            log.info("共查询到{}条记录", total);

            List<Hit<Song>> hits = response.hits().hits();
            for (Hit<Song> hit : hits) {
                Map<String, List<String>> highlight = hit.highlight();



                Song source = hit.source();


                // 覆盖非高亮结果
                if(highlight.containsKey("artistName")){
                    source.setArtistName(highlight.get("artistName").toString());
                }
                if(highlight.containsKey("album")){
                    source.setAlbum(highlight.get("album").toString());
                }
                if(highlight.containsKey("lyrics")){
                    source.setLyricsSegment(highlight.get("lyrics").get(0));
                }
                if (highlight.containsKey("songName")) {
                    source.setSongName(highlight.get("songName").toString());
                }
                if (highlight.containsKey("style")) {
                    source.setStyle(highlight.get("style").toString());
                }

                log.info("处理后的文本：{}",source.toString());


            }
        }


    }




}
