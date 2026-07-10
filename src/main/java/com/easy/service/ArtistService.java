package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistDTO;
import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.Artist;
import com.easy.result.PageResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ArtistService extends IService<Artist> {
    PageResult page(@Valid ArtistPageQueryDTO pageQueryDTO);


    void add(ArtistDTO artistDTO);

    void update(ArtistDTO artistDTO);

    void updateAvatar(Long artistId, MultipartFile avatar);

    void delete(Long artistId);

    void deleteByIds(List<Long> artistIds);

    Long getCount(Integer gender, String area);

    List<Map<String,Object>> getNames();

    void certify(Long artistId);

    PageResult search(@NotBlank String artistName, @Valid PageQueryDTO pageQueryDTO);
}
