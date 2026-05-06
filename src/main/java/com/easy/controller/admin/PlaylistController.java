package com.easy.controller.admin;

import com.easy.pojo.dto.*;
import com.easy.pojo.vo.PlaylistSongVO;
import com.easy.pojo.vo.PlaylistVO;
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
@RequestMapping("/admin")
@Tag(name = "歌单管理接口")
@RequiredArgsConstructor
public class  PlaylistController {

    private final PlaylistService playlistService;

    @Operation(summary = "获取所有歌单数量接口")
    @GetMapping("/getAllPlaylistsCount")
    public Result<Long> getAllPlaylistsCount(
            @Parameter(description = "歌单风格", example = "null")
            @RequestParam(required = false) String style) {
        return playlistService.getAllPlaylistsCount(style);
    }


    @Operation(summary = "歌单分页查询接口")
    @PostMapping("/getAllPlaylists")
    public Result<PageResult<PlaylistVO>> getAllPlaylists(@RequestBody PlaylistPageQueryDTO pageQueryDTO) {
        return playlistService.getAllPlaylists(pageQueryDTO);
    }

    @Operation(summary = "新增歌单接口")
    @PostMapping("/addPlaylist")
    public Result addPlaylist(@RequestBody PlaylistAddDTO playlistAddDTO) {
        return playlistService.addPlaylist(playlistAddDTO);
    }

    @Operation(summary = "批量新增歌单歌曲接口")
    @PostMapping("/addPlaylistSongs")
    public Result addPlaylistSongs(@RequestBody PlaylistSongAddDTO addDTO) {
        return playlistService.addPlaylistSongs(addDTO);
    }

    @Operation(summary = "更新歌单信息接口")
    @PutMapping("/updatePlaylist")
    public Result updatePlaylist(@RequestBody PlaylistUpdateDTO playlistUpdateDTO) {
        return playlistService.updatePlaylist(playlistUpdateDTO);
    }

    @Operation(summary = "更新歌单封面接口")
    @PatchMapping("/updatePlaylistCover/{id}")
    public Result updatePlaylistCover(@PathVariable("id") Long playlistId, @RequestParam("cover") MultipartFile cover) {

        return playlistService.updatePlaylistCover(playlistId, cover);
    }


    @Operation(summary = "删除歌单接口")
    @DeleteMapping("/deletePlaylist/{id}")
    public Result deletePlaylist(@PathVariable("id") Long playlistId) {
        return playlistService.deletePlaylist(playlistId);
    }

    @Operation(summary = "批量删除歌单接口")
    @DeleteMapping("/deletePlaylists")
    public Result deletePlaylists(@RequestBody List<Long> playlistIds) {
        return playlistService.deletePlaylists(playlistIds);
    }

    @Operation(summary = "获取歌单歌曲内容")
    @PostMapping("/getPlaylistSongs")
    public Result<PageResult<PlaylistSongVO>> getPlaylistSongs(@RequestBody PlaylistSongPageQueryDTO pageQueryDTO) {
        return playlistService.getPlaylistSongs(pageQueryDTO);
    }

}
