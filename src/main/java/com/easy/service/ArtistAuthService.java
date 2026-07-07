package com.easy.service;

import com.easy.pojo.dto.ArtistAuditDTO;
import com.easy.pojo.dto.ArtistAuthPageQueryDTO;
import com.easy.pojo.entity.ArtistAuth;
import com.easy.result.PageResult;
import com.easy.result.Result;

public interface ArtistAuthService {

    Result<PageResult> getArtistAuth(ArtistAuthPageQueryDTO pageQueryDTO);

    Result deleteArtistAuth(Long id);

    Result updateArtistAuth(ArtistAuth auth);

    Result addArtistAuth(ArtistAuth auth);
}
