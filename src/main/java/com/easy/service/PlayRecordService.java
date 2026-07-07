package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.PlayRecord;
import com.easy.pojo.entity.Song;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public interface PlayRecordService extends IService<PlayRecord> {
    List<Song> listByUserId();

    void delete(Long songId);
}
