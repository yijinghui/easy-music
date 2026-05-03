package com.easy.service;

import com.easy.pojo.dto.ArtistAddDTO;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistUpdateDTO;
import com.easy.pojo.entity.Artist;
import com.easy.pojo.vo.ArtistNameVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ArtistService {
    Result<PageResult<Artist>> page(ArtistPageQueryDTO pageQueryDTO);

    Result addArtist(ArtistAddDTO artistAddDTO);

    Result updateArtist(ArtistUpdateDTO artistUpdateDTO);

    Result updateArtistAvatar(Long artistId, MultipartFile avatar);

    Result deleteArtist(Long artistId);

    Result deleteArtists(List<Long> artistIds);

    Result<Long> getAllArtistsCount(Integer gender, String area);

    Result<List<ArtistNameVO>> getAllArtistNames();
}
