package com.easy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.easy.pojo.dto.ArtistAuthPageQueryDTO;
import com.easy.pojo.entity.ArtistAuth;
import com.easy.result.PageResult;
import com.easy.result.Result;

public interface ArtistAuthService extends IService<ArtistAuth> {

    PageResult page(ArtistAuthPageQueryDTO pageQueryDTO);

    void audit(ArtistAuth auth);
}
