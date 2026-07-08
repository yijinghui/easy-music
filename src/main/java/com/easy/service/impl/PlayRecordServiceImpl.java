package com.easy.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.easy.mapper.PlayRecordMapper;
import com.easy.mapper.SongMapper;
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


@Service
@RequiredArgsConstructor
public class PlayRecordServiceImpl extends ServiceImpl<PlayRecordMapper, PlayRecord> implements PlayRecordService {

    private final SongMapper songMapper;

    @Override
    public List<Song> listByUserId() {
        Long userId = ThreadLocalUtil.getUserId();
        List<Long> ids = baseMapper.selectByUserId(userId);

        if (ids.isEmpty()) {
            return List.of();
        }

        // 2. 从歌曲表中查询歌曲信息
        return songMapper.getByIds(ids);

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
