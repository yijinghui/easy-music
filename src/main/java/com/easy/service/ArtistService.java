package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistDTO;
import com.easy.pojo.dto.SongDTO;
import com.easy.pojo.entity.Artist;
import com.easy.pojo.vo.ArtistNameVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArtistService extends IService<Artist> {
    PageResult page(@Valid ArtistPageQueryDTO pageQueryDTO);


    Result addArtist(ArtistDTO artistDTO);

    Result updateArtist(ArtistDTO artistDTO);

    Result updateArtistAvatar(Long artistId, MultipartFile avatar);

    Result deleteArtist(Long artistId);

    Result deleteArtists(List<Long> artistIds);

    Result<Long> getAllArtistsCount(Integer gender, String area);

    Result<List<ArtistNameVO>> getAllArtistNames();



    /**
     * 用户端更新歌手信息（需要验证权限）
     */
    Result updateArtistInfo(ArtistDTO artistDTO);


    void certify(Long artistId);


}
