package com.easy.controller.admin;


import com.easy.pojo.dto.ArtistAddDTO;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistUpdateDTO;
import com.easy.pojo.dto.ArtistAuthDTO;
import com.easy.pojo.entity.Artist;
import com.easy.pojo.vo.ArtistNameVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/admin")
@Tag(name = "Admin端-歌手管理接口")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    @Operation(summary = "歌手分页查询接口")
    @PostMapping("/getAllArtists")
    public Result<PageResult<Artist>> page(@RequestBody ArtistPageQueryDTO pageQueryDTO) {
        return artistService.page(pageQueryDTO);
    }

    @Operation(summary = "歌手添加接口")
    @PostMapping("/addArtist")
    public Result addArtist(@RequestBody ArtistAddDTO artistAddDTO) {
        return artistService.addArtist(artistAddDTO);
    }

    @Operation(summary = "更新歌手信息接口")
    @PutMapping("/updateArtist")
    public Result updateArtist(@RequestBody ArtistUpdateDTO artistUpdateDTO) {
        return artistService.updateArtist(artistUpdateDTO);
    }

    @Operation(summary = "更新歌手头像接口")
    @PatchMapping("/updateArtistAvatar/{id}")
    public Result updateArtistAvatar(@PathVariable("id") Long artistId, @RequestParam("avatar") MultipartFile avatar) {
        return artistService.updateArtistAvatar(artistId, avatar);
    }

    @Operation(summary = "删除歌手接口")
    @DeleteMapping("/deleteArtist/{id}")
    public Result deleteArtist(@PathVariable("id") Long artistId) {
        return artistService.deleteArtist(artistId);
    }


    @Operation(summary = "批量删除歌手接口")
    @DeleteMapping("/deleteArtists")
    public Result deleteArtists(@RequestBody List<Long> artistIds) {
        return artistService.deleteArtists(artistIds);
    }

    @Operation(summary = "获取所有歌手数量接口")
    @GetMapping("/getAllArtistsCount")
    public Result<Long> getAllArtistsCount(@RequestParam(required = false) Integer gender, @RequestParam(required = false) String area) {
        return artistService.getAllArtistsCount(gender, area);
    }

    @Operation(summary = "获取所有歌手id和名称接口")
    @GetMapping("/getAllArtistNames")
    public Result<List<ArtistNameVO>> getAllArtistNames() {
        return artistService.getAllArtistNames();
    }

    @Operation(summary = "歌手认证审核接口")
    @PostMapping("/artistAuth")
    public Result artistAuth(@RequestBody ArtistAuthDTO artistAuthDTO) {
        return artistService.artistAuth(artistAuthDTO);
    }



}
