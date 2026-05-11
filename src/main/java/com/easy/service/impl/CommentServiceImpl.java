package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;

import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.constant.MessageConstant;
import com.easy.mapper.CommentMapper;
import com.easy.pojo.dto.CommentPlaylistDTO;
import com.easy.pojo.dto.CommentScrollQueryDTO;
import com.easy.pojo.dto.CommentSongDTO;
import com.easy.pojo.entity.Comment;
import com.easy.pojo.vo.CommentInfoVO;
import com.easy.pojo.vo.CommentVO;
import com.easy.result.Result;
import com.easy.result.ScrollResult;
import com.easy.service.CommentService;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// ZSET（不设置过期时间）:
// 一级评论："commentCache:song"+songId+":0"
// 二级评论："commentCache:comment:"+commentId.toString()+":"+commentId.toString()

// HASH（设置过期时间：1h）:
// 评论详情（用户名、用户头像、评论内容）："commentCache:content:"+commentId

// SET（不设置过期时间，查询点赞数）
// 评论点赞用户列表："commentCache:like:"+commentId

// 1.1.使用ZSET进行混动分页查询、查看一级评论：通过ZSET（parent_id=0）获取一级评论索引ID


@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final CommentMapper commentMapper;

    private final StringRedisTemplate stringRedisTemplate;


    @Override
    public Result addSongComment(CommentSongDTO commentSongDTO) {

        Long userId = ThreadLocalUtil.getUserId();

        Comment comment = new Comment();

        BeanUtil.copyProperties(commentSongDTO, comment);

        comment.setUserId(userId).setType(0)
                .setCreateTime(LocalDateTime.now())
                .setLikeCount(0L);

        if (commentMapper.insert(comment) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }

        Long songId = commentSongDTO.getSongId();
        Long parentId = commentSongDTO.getParentId();
        Long rootId = commentSongDTO.getRootId();

        stringRedisTemplate.opsForZSet().add(
                "commentCache:song:"+songId.toString()+":"+(parentId==0?parentId:rootId),
                comment.getCommentId().toString(),
                System.currentTimeMillis());

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }


    @Override
    public Result addPlaylistComment(CommentPlaylistDTO commentPlaylistDTO) {
        return Result.success();

//        Long userId = ThreadLocalUtil.getUserId();
//
//        Comment comment = new Comment();
//
//        BeanUtil.copyProperties(commentPlaylistDTO, comment);
//
//        LocalDateTime now = LocalDateTime.now();
//        comment.setUserId(userId).setType(1)
//                .setCreateTime(now)
//                .setLikeCount(0L);
//
//        if (commentMapper.insert(comment) == 0) {
//            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
//        }
//
//
//        stringRedisTemplate.opsForZSet().add(
//                "commentCache:playlist:"+commentPlaylistDTO.getPlaylistId(),
//                comment.getCommentId().toString(),
//                System.currentTimeMillis());
//
//        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    @Override
    public Result likeComment(Long commentId, Integer likeStatus) {

        Long userId = ThreadLocalUtil.getUserId();


        Comment comment = commentMapper.selectById(commentId);

        if (comment == null) {
            return Result.error("评论不存在");
        }

        String key = "commentCache:like:"+commentId;


        if (likeStatus == 0){
            Long result = stringRedisTemplate.opsForSet().remove(key, userId.toString());
            if (result==null || result==0) return Result.error("取消点赞失败");
        }else {
            Long result = stringRedisTemplate.opsForSet().add(key, userId.toString());
            if (result==null || result==0) return Result.error("点赞失败");
        }

        return Result.success();


    }



    @Override
    public Result<ScrollResult<CommentVO>> listSongComment(CommentScrollQueryDTO queryDTO) {


        // 存在慢查询（查看慢查询日志得知），但是接口平均响应时间得到大幅提升（240ms-4ms）,可优化SCARD（使用字符串类型存储点赞个数）
        // 使用pipeline代替lus

        Long userId = ThreadLocalUtil.getUserId();

        Long songId = queryDTO.getId();
        Long maxTime = queryDTO.getMaxTime();
        Integer offset = queryDTO.getOffset();
        Long rootId = queryDTO.getRootId();

        String key = "commentCache:song:" + songId.toString()+":"+rootId;


        Set<ZSetOperations.TypedTuple<String>> typedTuples;

        if(rootId>0){
            typedTuples = stringRedisTemplate.opsForZSet().
                    rangeByScoreWithScores(
                            key, 0, maxTime, offset, 15
                    );
        }else {
            typedTuples = stringRedisTemplate.opsForZSet().
                    reverseRangeByScoreWithScores(
                            key, 0, maxTime, offset, 15
                    );
        }

        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.success(new ScrollResult<>());
        }


        long minTime=0L;
        int os = 0;

        List<Long> commentIds = new ArrayList<>();
        // 解析 typedTuples
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples){
            commentIds.add(Long.valueOf(tuple.getValue()));

            // 获取分数（时间戳）
            long time = tuple.getScore().longValue();

            if (time==minTime){
                os++;
            }else {
                minTime=time;
                os=0;
            }
        }

        // 批量查询用户信息
        List<String> infoStrList = stringRedisTemplate.opsForValue().multiGet(
                commentIds.stream().map(commentId -> "commentCache:content:" + commentId).collect(Collectors.toList())
        );

        List<CommentInfoVO> commentInfos = infoStrList.stream()
                .map(infoStr -> JSONUtil.toBean(infoStr, CommentInfoVO.class))
                .collect(Collectors.toList());

        List<Long> notExistCommentIds = new ArrayList<>();
        List<Integer> notExistCommentIndex = new ArrayList<>();

        List<String> contentKeys = new ArrayList<>();
        List<String> likeKeys = new ArrayList<>();

        for (int i=0;i<commentIds.size();i++){
            contentKeys.add("commentCache:content:" + commentIds.get(i));
            likeKeys.add("commentCache:like:" + commentIds.get(i));
            if (infoStrList.get(i) == null) {
                notExistCommentIds.add(commentIds.get(i));
                notExistCommentIndex.add(i);
            }
        }


        if (!notExistCommentIds.isEmpty()){
            List<CommentInfoVO> notExistCommentInfos = commentMapper.selectCommentInfos(notExistCommentIds);

            // 使用lua脚本批量缓存至Redis中
            String luaScript1 =
                    "for i = 1, #KEYS do " +
                            "local key = KEYS[i] " +
                            "local value = ARGV[i] " +
                            "redis.call('SETEX', key, 1800, value) " +
                            "end " +
                            "return #KEYS";


           String[] args = new String[notExistCommentInfos.size()];


            for (int i = 0; i < notExistCommentInfos.size(); i++) {
                args[i] = JSONUtil.toJsonStr(notExistCommentInfos.get(i));
                Integer index = notExistCommentIndex.get(i);
                commentInfos.set(index, notExistCommentInfos.get(i));
            }

            DefaultRedisScript<Long> redisScript1 = new DefaultRedisScript<>();
            redisScript1.setScriptText(luaScript1);
            redisScript1.setResultType(Long.class);


        }

        List<CommentVO> commentVOList = commentInfos.stream().map(info -> {
            CommentVO commentVO = new CommentVO();
            BeanUtil.copyProperties(info, commentVO);
            return commentVO;
        }).toList();


        String luaScript2 =
                "local userId = ARGV[1] " +
                        "local result = '' " +
                        "for i = 1, #KEYS do " +
                        "    local likeCount = redis.call('SCARD', KEYS[i]) " +
                        "    local isLike = redis.call('SISMEMBER', KEYS[i], userId) " +
                        "    if i > 1 then " +
                        "        result = result .. ';' " +
                        "    end " +
                        "    result = result .. likeCount .. ',' .. isLike " +
                        "end " +
                        "return result";

        DefaultRedisScript<String> redisScript2 = new DefaultRedisScript<>();
        redisScript2.setScriptText(luaScript2);
        redisScript2.setResultType(String.class);

        String userIdStr = userId.toString();
        String result = stringRedisTemplate.execute(redisScript2, likeKeys, userIdStr);
        log.info("execute:{}", result);
        String[] split = result.split(";");
        for (int i = 0; i < split.length; i++) {
            commentVOList.get(i).setLikeCount(Long.valueOf(split[i].split(",")[0]));
            commentVOList.get(i).setIsLike(Integer.parseInt(split[i].split(",")[1]) == 1);
        }




        ScrollResult<CommentVO> scrollResult = new ScrollResult<>();
        scrollResult.setList(commentVOList);
        scrollResult.setOffset(os);
        scrollResult.setMinTime(StrUtil.toString(minTime));

        return Result.success(scrollResult);

    }


    @Override
    @CacheEvict(value = "commentCache:content:", key = "#commentId")
    public Result deleteComment(Long commentId) {

        Long userId = ThreadLocalUtil.getUserId();

        Comment comment = commentMapper.selectById(commentId);

        if (comment == null) {
            return Result.error("评论不存在");
        }

        if (!Objects.equals(comment.getUserId(), userId)) {
            return Result.error("您没有权限删除此评论");
        }

        if (commentMapper.deleteById(commentId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        // 删除评论ZSET
        String key = "commentCache:song:" + comment.getSongId() +":"+ comment.getParentId();
        stringRedisTemplate.opsForZSet().remove(key, commentId.toString());


        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }
}
