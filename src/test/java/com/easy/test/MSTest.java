package com.easy.test;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.easy.config.MeilisearchProperties;
import com.easy.mapper.SongMapper;
import com.easy.mapper.UserFavoriteMapper;
import com.easy.pojo.entity.Song;

import com.easy.utils.MSUtil;
import com.easy.utils.ThreadLocalUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.meilisearch.sdk.Client;
import com.meilisearch.sdk.Index;
import com.meilisearch.sdk.SearchRequest;
import com.meilisearch.sdk.model.SearchResult;

import com.meilisearch.sdk.model.TaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MSTest {

    @Autowired
    private Client client;
    @Autowired
    private SongMapper songMapper;
    @Autowired
    private UserFavoriteMapper  userFavoriteMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private MeilisearchProperties meilisearchProperties;

    @Autowired
    private MSUtil msUtil;

    @Test
    void testCreateIndex() {
        System.out.println("测试创建索引");
        client.createIndex("music");
        System.out.println("索引创建完成");
        client.deleteIndex("music");
        System.out.println("索引删除完成");
    }

    @Test
    void testSearchIndex() {
        String indexUid = "music";

        // 删除旧索引（如果存在）
        try {
            client.deleteIndex(indexUid);
            System.out.println("旧索引已删除");
        } catch (Exception e) {
            // 索引不存在，忽略
        }

        // 创建新索引，songId 作为主键
        TaskInfo taskInfo = client.createIndex(indexUid, "songId");
        System.out.println("索引创建中... Task UID: " + taskInfo.getTaskUid());

        Index index = client.index(indexUid);

        // 1. 设置可搜索属性（对应 ES 中被分析的 text 字段）
        index.updateSearchableAttributesSettings(new String[]{
                "songName",      // text 类型，ik_smart 分词
                "artistName",    // keyword 转 text 用于搜索
                "album",         // text 类型，ik_smart 分词
                "lyricsSegment",        // text 类型，ik_smart 分词
                "style",         // text 类型，ik_smart 分词
        });

        // 2. 设置可过滤属性（对应 ES 的 keyword 类型字段）
        index.updateFilterableAttributesSettings(new String[]{
                "songId",        // keyword
                "artistName",    // keyword，精确过滤
                "album",         // 同时支持过滤
                "style",         // 同时支持过滤
        });

        index.updateSortableAttributesSettings(new String[]{
                "songId"
        });


        // 4. 设置显示的属性
        index.updateDisplayedAttributesSettings(new String[]{
                "songId",
                "songName",
                "artistName",
                "album",
                "lyricsSegment",
                "style"
        });

        // 5. 设置停用词库（中文）
        // Meilisearch 会自动处理中文分词
        index.updateStopWordsSettings(new String[]{
                "的", "一", "不", "在", "人", "了", "有", "是", "为", "这", "就", "个", "也", "上", "和", "大",
                "我", "你", "他", "她", "它", "们", "那", "些", "会", "要", "到", "说", "中", "来", "没", "去",
                "都", "能", "把", "着", "与", "很", "自己", "之", "将", "对", "可以", "这个", "还", "从", "而",
                "被", "所以", "但", "以", "及", "或", "啊", "吧", "呢", "吗", "哦", "嗯", "哈", "呀", "哇", "呵",
                "么", "如", "想", "做", "看", "让", "只", "过", "用", "比", "又", "好", "小", "多", "后", "前",
                "其", "她", "它", "所", "得", "可", "等", "太", "最", "更", "已", "因为", "如果", "但是", "不是",
                "没有", "我们", "你们", "他们", "她们", "自己", "什么", "怎么", "怎样", "如何", "为什么", "哪",
                "哪里", "哪个", "这种", "那种", "这样", "那样", "这里", "那里", "时候", "的话", "一样", "还是",
                "然后", "而且", "或者", "虽然", "不过", "可能", "应该", "已经", "一直", "这些", "那些", "一切",
                "所有", "现在", "非常", "可以", "需要", "知道", "觉得", "起来", "出来", "过来", "回来", "上去",
                "下去", "不会", "不能", "不要", "别", "各位", "大家", "各位", "一点", "一下", "一个", "一种",
                "每个", "整个", "另外", "除了", "关于", "对于", "由于", "根据", "通过", "经过", "为了", "按照",
                "除了", "有关", "似的", "一样", "一般", "一起", "一直", "一面", "万一", "之后", "之前", "之间",
                "之内", "之外", "之中", "之一", "只是", "而是", "不是", "就是", "还是", "总是", "于是", "终于",
                "当然", "忽然", "接着", "后来", "同时", "并且", "而且", "然而", "因此", "此外", "从而", "反而",
                "无论", "以及", "而且", "接着", "等等", "例如", "比如", "甚至", "尤其", "至少", "最后", "立刻",
                "突然", "经常", "往往", "不断", "原来", "当然", "必须", "并且", "虽然", "仍然", "是否", "然后",
                "所谓", "相对", "几乎", "到处", "重新", "完全", "根本", "绝对", "极", "极其", "相当", "比较",
                "着", "了", "过", "的", "得", "地", "啊", "吧", "吗", "呢", "哦", "嗯", "呀", "哈", "呵", "嘿",
                "嗯", "哦", "哎", "唉", "哟", "咦", "喂", "啦", "呗", "咯", "嘛", "噢", "咳", "哇", "嘻",
                "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", ":", ";", "<",
                "=", ">", "?", "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~", "！", "＂", "＃",
                "＄", "％", "＆", "＇", "（", "）", "＊", "＋", "，", "－", "．", "／", "：", "；", "＜", "＝",
                "＞", "？", "＠", "［", "＼", "］", "＾", "＿", "｀", "｛", "｜", "｝", "～", "“", "”", "‘",
                "’", "【", "】", "《", "》", "「", "」", "『", "』", "〖", "〗", "〔", "〕", "（", "）", "｛",
                "｝", "〈", "〉", "…", "—", "～", "·", "　", " ", "0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H",
                "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z",
                "the", "a", "an", "is", "are", "am", "was", "were", "be", "been", "being", "have", "has",
                "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can",
                "shall", "to", "of", "in", "for", "on", "with", "at", "by", "from", "as", "into", "through",
                "during", "before", "after", "above", "below", "between", "and", "but", "or", "not",
                "this", "that", "these", "those", "it", "its", "i", "me", "my", "we", "our", "you", "your",
                "he", "him", "his", "she", "her", "they", "them", "their", "what", "which", "who", "how",
                "when", "where", "if", "then", "else", "so", "than", "too", "very", "just", "about", "also"
        });

        System.out.println("索引设置完成！");
    }



    @Test
    public void buildDocument() throws JsonProcessingException {
        // 查询所有歌曲
        List<Song> songs = songMapper.selectList(null);

        // 转换为 Meilisearch 文档格式
        List<Map<String, Object>> documents = new ArrayList<>();
        for (Song song : songs) {
            Map<String, Object> doc = new HashMap<>();
            doc.put("songId", song.getSongId().toString());
            doc.put("songName", song.getSongName());
            doc.put("artistName", song.getArtistName());
            doc.put("album", song.getAlbum());
            doc.put("lyricsSegment", song.getLyrics());

            doc.put("style", song.getStyle());
            documents.add(doc);
        }

        // 批量导入到 Meilisearch
        ObjectMapper mapper = new ObjectMapper();
        // 注册 Java 8 时间模块
        mapper.registerModule(new JavaTimeModule());
        // 禁用将日期写为时间戳
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Index index = client.index("music");
        TaskInfo task = index.addDocuments(mapper.writeValueAsString(documents), "songId");
        System.out.println("导入任务ID: " + task.getTaskUid());
        System.out.println("共导入 " + songs.size() + " 条记录");
    }

    @Test
    public void testGetDocument() {
        Index index = client.index("music");

        // 全文搜索
        String keyword = "晴天"; // 你要搜索的关键词


        SearchResult result = index.search(keyword);

        System.out.println("搜索 '" + keyword + "' 找到 " + result.getEstimatedTotalHits() + " 条结果:");

        for (int i = 0; i < result.getHits().size(); i++) {
            Map<String, Object> doc = (Map<String, Object>) result.getHits().get(i);
            System.out.println("\n结果 " + (i+1) + ":");
            System.out.println("  歌曲: " + doc.get("songName"));
            System.out.println("  歌手: " + doc.get("artistName"));
            System.out.println("  专辑: " + doc.get("album"));
            System.out.println("  风格: " + doc.get("style"));
            System.out.println("  songId: " + doc.get("songId"));
            System.out.println(doc.toString());
        }
    }


    @Test
    public void testSearchDocument() {
        List<Object> result = msUtil.search("music", "故事的小黄花", Arrays.asList("songName", "artistName", "album", "style", "lyricsSegment"));

        // 转化为Song对象
        List<Song> songs = result.stream()
                .map(obj -> {
                    // 先将对象转为标准 JSON
                    String jsonStr = JSONUtil.toJsonStr(obj);
                    return JSONUtil.toBean(jsonStr, Song.class);
                })
                .toList();

        // 查询歌曲的完整数据
        if (songs.isEmpty()) return;
        List<Song> fullSongs = songMapper.getByIds(songs.stream().map(Song::getSongId).toList());

        // 用户是否收藏
        Long userId = ThreadLocalUtil.getUserId();
        Set<Long> ids = userFavoriteMapper.getUserFavoriteSongIds(userId);

        // 合并完整数据
        for (int i = 0; i < fullSongs.size(); i++) {
            fullSongs.get(i).setSongName(songs.get(i).getSongName());
            fullSongs.get(i).setArtistName(songs.get(i).getArtistName());
            fullSongs.get(i).setAlbum(songs.get(i).getAlbum());
            fullSongs.get(i).setStyle(songs.get(i).getStyle());
            fullSongs.get(i).setLyricsSegment(songs.get(i).getLyricsSegment());
            if (ids.contains(fullSongs.get(i).getSongId())) {
                fullSongs.get(i).setIsFavorite(true);
            }
            log.info("song: {}", fullSongs.get(i));
        };

    }


    @Test
    public void testUpdateDocument() {
        Index index = client.index("music");

        // 批量更新
        List<Map<String, Object>> documents = new ArrayList<>();

        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("songId", "548");
        doc1.put("lyricsSegment", "故事的小黄花,从出生那年就飘着,童年的荡秋千,随记忆一直晃到现在,Re So So Si Do Si La,So La Si Si Si Si La Si La So,吹着前奏望着天空,我想起花瓣试着掉落,为你翘课的那一天,花落的那一天,教室的那一间,我怎么看不见,消失的下雨天,我好想再淋一遍,没想到失去的勇气我还留着,好想再问一遍,你会等待还是离开,刮风这天我试过握着你手,但偏偏雨渐渐大到我看你不见,还要多久我才能在你身边,等到放晴的那天也许我会比较好一点,从前从前有个人爱你很久,但偏偏风渐渐把距离吹得好远,好不容易又能再多爱一天,但故事的最后你好像还是说了拜拜,为你翘课的那一天,花落的那一天,教室的那一间,我怎么看不见,消失的下雨天,我好想再淋一遍,没想到失去的勇气我还留着,好想再问一遍,你会等待还是离开,刮风这天我试过握着你手,但偏偏雨渐渐大到我看你不见,还要多久我才能在你身边,等到放晴的那天也许我会比较好一点,从前从前有个人爱你很久,偏偏风渐渐把距离吹得好远,好不容易又能再多爱一天,但故事的最后你好像还是说了拜拜,刮风这天我试过握着你手,但偏偏雨渐渐大到我看你不见,还要多久我才能够在你身边,等到放晴那天也许我会比较好一点,从前从前有个人爱你很久,但偏偏雨渐渐把距离吹得好远,好不容易又能再多爱一天,但故事的最后你好像还是说了拜");

        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", "456");
        doc2.put("lyricsSegment", "更新后的歌词2");

        documents.add(doc1);
        documents.add(doc2);

        // 使用 JSONUtil 序列化
        String jsonStr = JSONUtil.toJsonStr(documents);
        index.updateDocuments(jsonStr);

    }

    @Test
    public void testCheckDocument() {
        Index index = client.index("music");

        // 查看当前文档结构
        String rawDocuments = index.getRawDocuments();
        JSONObject jsonObject = JSONUtil.parseObj(rawDocuments);
    }
}
