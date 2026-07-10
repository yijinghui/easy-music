package com.easy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.easy.mapper.*;
import com.easy.pojo.entity.Playlist;
import com.easy.pojo.entity.UserFavorite;
import com.easy.pojo.vo.UserStatVO;
import com.easy.result.Result;
import com.easy.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final UserMapper userMapper;


    private final UserFavoriteMapper userFavoriteMapper;


    private final PlaylistMapper playlistMapper;


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

        // 统计用户创建的歌单数
        Long createdPlaylistCount = playlistMapper.selectCount(new LambdaQueryWrapper<Playlist>()
                .eq(Playlist::getUserId, userId)
        );



        userStatVO.setFavoriteSongCount(favoriteSongCount.intValue());
        userStatVO.setFavoritePlaylistCount(favoritePlaylistCount.intValue());
        userStatVO.setCreatedPlaylistCount(createdPlaylistCount.intValue());

        return userStatVO;
    }

    @Override
    public Result<Map<String, Object>> getUserStatistic(Integer days) {
        if (days == null || days <= 0) {
            return Result.error("参数错误", null);
        }

        // 1. 生成日期范围
        LocalDate end = LocalDate.now();
        LocalDate begin = LocalDate.now().minusDays(days - 1);

        // 2. 查询数据库（只返回有数据的日期）
        List<Map<String, Object>> dailyNewUsers = userMapper.getDailyNewUsers(begin, end);

        // 3. 将查询结果转为 Map，便于快速查找
        Map<LocalDate, Long> dataMap = new HashMap<>();
        for (Map<String, Object> map : dailyNewUsers) {
            LocalDate date = LocalDate.parse(map.get("date").toString());
            Long count = Long.parseLong(map.get("count").toString());
            dataMap.put(date, count);
        }

        // 4. 补全所有日期（包括0值的日期）
        List<LocalDate> dateList = new ArrayList<>();
        List<Long> countList = new ArrayList<>();

        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
            countList.add(dataMap.getOrDefault(date, 0L));  // 没有数据则补0
        }

        // 5. 返回结果
        Map<String, Object> result = Map.of("date", dateList, "count", countList);
        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getSongPlayData(Integer days) {
        /*if (days == null || days <= 0) {
            return Result.error("参数错误", null);
        }

        // 1. 生成日期范围
        LocalDate end = LocalDate.now();
        LocalDate begin = LocalDate.now().minusDays(days - 1);

        // 2. 查询数据库（只返回有数据的日期）
        List<Map<String, Object>> dailyPlayData = playRecordMapper.dailyPlayData(begin, end);

        // 3. 将查询结果转为 Map，便于快速查找
        Map<LocalDate, Long> dataMap = new HashMap<>();
        for (Map<String, Object> map : dailyPlayData) {
            LocalDate date = LocalDate.parse(map.get("date").toString());
            Long count = Long.parseLong(map.get("count").toString());
            dataMap.put(date, count);
        }

        // 4. 补全所有日期（包括0值的日期）
        List<LocalDate> dateList = new ArrayList<>();
        List<Long> countList = new ArrayList<>();

        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
            countList.add(dataMap.getOrDefault(date, 0L));  // 没有数据则补0
        }

        // 5. 返回结果
        Map<String, Object> result = Map.of("date", dateList, "count", countList);
        return Result.success(result);*/
        return Result.success(null);
    }
}
