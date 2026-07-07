package com.easy.controller.admin;

import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.*;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/playlists")
@Tag(name = "Admin端-歌单管理接口")
@RequiredArgsConstructor
public class  PlaylistController {

    private final PlaylistService playlistService;

    @Operation(summary = "获取所有歌单数量接口")
    @GetMapping("/count")
    public Result<Long> getAllPlaylistsCount(
            @Parameter(description = "歌单风格", example = "null")
            @RequestParam(required = false) String style) {
        return playlistService.getAllPlaylistsCount(style);
    }


    @Operation(summary = "歌单分页查询接口")
    @PostMapping("/page")
    public Result<PageResult> getAllPlaylists(@RequestBody PlaylistPageQueryDTO pageQueryDTO) {
        return playlistService.getAllPlaylists(pageQueryDTO);
    }

    @Operation(summary = "新增歌单接口")
    @PostMapping("/create")
    @LogOperation
    public Result addPlaylist(@RequestBody PlaylistDTO playlistDTO) {
        playlistService.create(playlistDTO);
        return Result.success();
    }

    @Operation(summary = "批量新增歌单歌曲接口")
    @PostMapping("/songs/{playlistId}")
    @LogOperation
    public Result addPlaylistSongs(@PathVariable Long playlistId,@RequestParam List<Long> songIds) {
        playlistService.addPlaylistSongs(playlistId,songIds);
        return Result.success();
    }



    @Operation(summary = "更新歌单信息接口")
    @PutMapping("/update")
    @LogOperation
    public Result updatePlaylist(@RequestBody PlaylistDTO playlistDTO) {
        return playlistService.updatePlaylist(playlistDTO);
    }

    @Operation(summary = "更新歌单封面接口")
    @PatchMapping("/cover/{id}")
    @LogOperation
    public Result updatePlaylistCover(@PathVariable("id") Long playlistId, @RequestParam("cover") MultipartFile cover) {

        return playlistService.updatePlaylistCover(playlistId, cover);
    }


    @Operation(summary = "删除歌单接口")
    @DeleteMapping("/{id}")
    @LogOperation
    public Result deletePlaylist(@PathVariable("id") Long playlistId) {
        return playlistService.deletePlaylist(playlistId);
    }

    @Operation(summary = "批量删除歌单接口")
    @DeleteMapping("/batch")
    @LogOperation
    public Result deletePlaylists(@RequestBody List<Long> playlistIds) {
        return playlistService.deletePlaylists(playlistIds);
    }

    @Operation(summary = "批量删除歌单接口")
    @DeleteMapping("/songs/{id}")
    @LogOperation
    public Result deletePlaylistSongs(@PathVariable ("id") Long playlistId ,@RequestBody List<Long> songIds) {
        return playlistService.deletePlaylistSongs(playlistId,songIds);
    }



}
