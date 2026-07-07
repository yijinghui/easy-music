package com.easy.service.impl;

import cn.hutool.core.bean.BeanUtil;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.exception.AccessDeniedException;
import com.easy.mapper.CommentMapper;
import com.easy.pojo.dto.CommentScrollQueryDTO;
import com.easy.pojo.dto.CommentDTO;
import com.easy.pojo.entity.Comment;
import com.easy.pojo.vo.CommentInfoVO;
import com.easy.result.ScrollResult;
import com.easy.service.CommentService;
import com.easy.utils.SensitiveWordUtil;
import com.easy.utils.ThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.easy.constant.MessageConstant.ACCESS_DENIED;
import static com.easy.constant.RedisConstant.COMMENT_LIKE_KEY;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SensitiveWordUtil sensitiveWordUtil;

    public void add(Long songId, Long playlistId, CommentDTO commentDTO) {

        if (songId == null && playlistId == null || songId != null && playlistId != null) {
            throw new IllegalArgumentException("评论对象不合法");
        }

        Long userId = ThreadLocalUtil.getUserId();
        // 过滤铭感内容
        commentDTO.setContent(sensitiveWordUtil.filter(commentDTO.getContent()));

        Comment comment = new Comment();
        BeanUtil.copyProperties(commentDTO, comment);
        comment.setUserId(userId);

        if (comment.getRootId() == 0) {
            comment.setIsHot(1);
        }

        if (songId != null) {
            comment.setType(0);
            comment.setSongId(songId);
        } else{
            comment.setType(1);
            comment.setPlaylistId(playlistId);
        }

        save(comment);
    }

    @Override
    public void delete(Long commentId) {
        Long userId = ThreadLocalUtil.getUserId();
        Comment comment = getById(commentId);
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new AccessDeniedException(ACCESS_DENIED);
        }
        removeById(commentId);

    }

    @Override
    public void likeComment(Long commentId, Integer likeStatus) {

        Long userId = ThreadLocalUtil.getUserId();

        String key = COMMENT_LIKE_KEY+commentId;

        if (likeStatus == 0){
            stringRedisTemplate.opsForSet().remove(key, userId.toString());
            //更新数据库
            update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .setSql("like_count = like_count-1")
            );
        }else if (likeStatus == 1){
            stringRedisTemplate.opsForSet().add(key, userId.toString());
            update(null, new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getId, commentId)
                    .setSql("like_count = like_count+1")
            );
        }

    }


    @Override
    public ScrollResult list(Long songId, Long playlistId,
                             CommentScrollQueryDTO queryDTO) {
        if (songId == null && playlistId == null || songId != null && playlistId != null) {
            throw new IllegalArgumentException("评论对象不合法");
        }
        // 1.判断是否查询的是一级评论
        Long rootId = queryDTO.getRootId();
        Long firstId = queryDTO.getFirstId();

        List<CommentInfoVO> hotCommentInfos= new ArrayList<>();
        if (rootId == 0&&firstId == 0) {
            // 1.获取一级评论中的热评
            hotCommentInfos = baseMapper.selectHotComment(songId,playlistId);
        }

        // 滚动分页查询评论信息
        List<CommentInfoVO> commentInfoVOS = baseMapper.scrollPageComment(songId, playlistId, rootId, firstId);

        // 合并结果
        List<CommentInfoVO> result = new ArrayList<>(hotCommentInfos);
        result.addAll(commentInfoVOS);

        // 获取用户是否点过赞、评论的子评论个数
        Long userId = ThreadLocalUtil.getUserId();
        if (userId != null) {
            for (CommentInfoVO commentInfoVO : result){
                String key = COMMENT_LIKE_KEY+commentInfoVO.getCommentId();
                commentInfoVO.setIsLiked(stringRedisTemplate.opsForSet().isMember(key, userId.toString()));
            }

        }

        Long lastId = !commentInfoVOS.isEmpty() ? commentInfoVOS.get(commentInfoVOS.size() - 1).getCommentId() : -1;
        return new ScrollResult(result, lastId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refreshHotComment() {
        LambdaUpdateWrapper<Comment> pw1 = new LambdaUpdateWrapper<Comment>()
                .set(Comment::getIsHot, 0)
                .lt(Comment::getLikeCount, 1000)
                .eq(Comment::getIsHot, 1);
        update(null, pw1);

        List<Comment> comments = list(
                new QueryWrapper<Comment>()
                        .ge("create_time", LocalDateTime.now().minusDays(7))
        );

        List<Long> hotIds = comments.stream()
                .filter(c -> calculateHotScore(c.getLikeCount(), c.getCreateTime()) >= 0.97)
                .map(Comment::getId)
                .toList();

        if (!hotIds.isEmpty()) {
            UpdateWrapper<Comment> updateWrapper = new UpdateWrapper<Comment>()
                    .set("is_hot", 1)
                    .in("id", hotIds);
            update(null, updateWrapper);
        }
    }

    // 热度计算公式
    private double calculateHotScore(Long likeCount, LocalDateTime createTime) {
        double hours = ChronoUnit.HOURS.between(createTime, LocalDateTime.now());
        double timeScore = Math.pow(0.5, hours / 12.5);
        double likeScore = Math.log(likeCount + 1) / Math.log(3) + 1; // LOG10
        return likeScore * timeScore;
    }



}
