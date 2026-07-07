package com.easy.controller.admin;


import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistDTO;
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
@RequestMapping("/admin/artists")
@Tag(name = "Admin端-歌手管理接口")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    @Operation(summary = "歌手分页查询接口")
    @PostMapping("/page")
    public Result<PageResult> getAllArtists(@RequestBody ArtistPageQueryDTO pageQueryDTO) {
        return Result.success(artistService.page(pageQueryDTO));
    }

    @Operation(summary = "歌手添加接口")
    @PostMapping("/create")
    @LogOperation
    public Result addArtist(@RequestBody ArtistDTO artistDTO) {
        return artistService.addArtist(artistDTO);
    }

    @Operation(summary = "更新歌手信息接口")
    @PutMapping("/update")
    @LogOperation
    public Result updateArtist(@RequestBody ArtistDTO artistDTO) {
        return artistService.updateArtist(artistDTO);
    }

    @Operation(summary = "更新歌手头像接口")
    @LogOperation
    @PatchMapping("/avatar{id}")
    public Result updateArtistAvatar(@PathVariable("id") Long artistId, @RequestParam("avatar") MultipartFile avatar) {
        return artistService.updateArtistAvatar(artistId, avatar);
    }

    @Operation(summary = "删除歌手接口")
    @DeleteMapping("/{id}")
    @LogOperation
    public Result deleteArtist(@PathVariable("id") Long artistId) {
        return artistService.deleteArtist(artistId);
    }


    @Operation(summary = "批量删除歌手接口")
    @DeleteMapping("/batch")
    @LogOperation
    public Result deleteArtists(@RequestBody List<Long> artistIds) {
        return artistService.deleteArtists(artistIds);
    }

    @Operation(summary = "获取所有歌手数量接口")
    @GetMapping("/count")
    public Result<Long> getAllArtistsCount(@RequestParam(required = false) Integer gender, @RequestParam(required = false) String area) {
        return artistService.getAllArtistsCount(gender, area);
    }

    @Operation(summary = "获取所有歌手id和名称接口")
    @GetMapping("/names")
    public Result<List<ArtistNameVO>> getAllArtistNames() {
        return artistService.getAllArtistNames();
    }







}
