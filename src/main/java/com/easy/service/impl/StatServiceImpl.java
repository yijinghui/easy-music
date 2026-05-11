package com.easy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easy.mapper.FollowMapper;
import com.easy.mapper.UserFavoriteMapper;
import com.easy.mapper.UserMapper;
import com.easy.pojo.entity.Follow;
import com.easy.pojo.entity.UserFavorite;
import com.easy.pojo.vo.UserStatVO;
import com.easy.service.StatService;
import com.easy.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class StatServiceImpl implements StatService {

    private final UserMapper userMapper;

    private final FollowMapper followMapper;

    private final UserFavoriteMapper userFavoriteMapper;

    private final StringRedisTemplate stringRedisTemplate;



    @Override
    public UserStatVO getUserStat(Long userId) {

        UserStatVO userStatVO = new UserStatVO();

        // 统计用户收藏的歌曲数
        Long favoriteSongCount = userFavoriteMapper.selectCount(new QueryWrapper<UserFavorite>()
                .eq("user_id", userId)
                .isNotNull("song_id"));

        // 统计用户收藏的歌单数
        Long favoritePlaylistCount = userFavoriteMapper.selectCount(new QueryWrapper<UserFavorite>()
                .eq("user_id", userId)
                .isNotNull("playlist_id"));

        // 统计用户的关注数
        Long followCount = followMapper.selectCount(new QueryWrapper<Follow>()
                .eq("follower_id", userId)
                .eq("status", 1)
        );

        // 统计用户粉丝数
        Long fansCount = followMapper.selectCount(new QueryWrapper<Follow>()
                .eq("following_id", userId)
                .eq("status", 1)
        );

        // 统计用户访客数
        Long visitorCount = stringRedisTemplate.opsForSet().size("visitor:user:" + userId);
        if (visitorCount == null) {
            visitorCount = 0L;
        }

        userStatVO.setFavoriteSongCount(favoriteSongCount.intValue());
        userStatVO.setFavoritePlaylistCount(favoritePlaylistCount.intValue());
        userStatVO.setFollowCount(followCount);
        userStatVO.setFansCount(fansCount);
        userStatVO.setVisitorCount(visitorCount);

        return userStatVO;
    }
}
