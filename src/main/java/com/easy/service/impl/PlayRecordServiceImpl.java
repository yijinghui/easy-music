package com.easy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.PlayRecordMapper;
import com.easy.mapper.SongMapper;
import com.easy.mapper.UserFavoriteMapper;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.PlayRecord;
import com.easy.pojo.entity.Song;
import com.easy.service.PlayRecordService;
import com.easy.service.SongService;
import com.easy.utils.ThreadLocalUtil;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class PlayRecordServiceImpl extends ServiceImpl<PlayRecordMapper, PlayRecord> implements PlayRecordService {

    private final SongMapper songMapper;
    private final UserFavoriteMapper userFavoriteMapper;

    @Override
    public List<Song> listByUserId() {
        Long userId = ThreadLocalUtil.getUserId();
        List<Long> ids = baseMapper.selectByUserId(userId);

        if (ids.isEmpty()) {
            return List.of();
        }

        List<Song> songs = songMapper.getByIds(ids);
        // 设置用户是否收藏
        Set<Long> favoriteSongIds = userFavoriteMapper.getUserFavoriteSongIds(userId);
        songs.forEach(song -> {
            if(favoriteSongIds.contains(song.getSongId())) {
                song.setIsFavorite(true);
            }
        });

        // 2. 从歌曲表中查询歌曲信息
        return songs;

    }

    @Override
    public void delete(Long songId) {
        Long userId = ThreadLocalUtil.getUserId();
        lambdaUpdate().eq(PlayRecord::getSongId, songId)
                .eq(PlayRecord::getUserId, userId)
                .set(PlayRecord::getIsDeleted, 1)
                .update();
    }
}
