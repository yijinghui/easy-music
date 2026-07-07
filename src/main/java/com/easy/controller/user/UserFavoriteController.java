package com.easy.controller.user;

import com.easy.pojo.dto.PageQueryDTO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.UserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Tag(name = "C端-用户收藏相关接口")
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @PostMapping("/songs")
    @Operation(summary = "获取用户收藏歌曲")
    public Result<PageResult> getUserFavoriteSongs(@RequestBody @Valid PageQueryDTO pageQueryDTO) {
        PageResult pageResult = userFavoriteService.getUserFavoriteSongs(pageQueryDTO);
        return Result.success(pageResult);
    }

    @PostMapping("/songs/{songId}")
    @Operation(summary = "收藏歌曲")
    public Result collectSong(@PathVariable Long songId) {
        userFavoriteService.collectSong(songId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/songs/{songId}")
    @Operation(summary = "取消收藏歌曲")
    public Result cancelCollectSong(@PathVariable Long songId) {
        userFavoriteService.cancelCollectSong(songId);
        return Result.success("取消收藏成功");
    }

    @PostMapping("/playlists")
    @Operation(summary = "获取用户收藏歌单")
    public Result<PageResult> getFavoritePlaylists(@RequestBody @Valid PageQueryDTO pageQueryDTO) {
        return Result.success(userFavoriteService.getUserFavoritePlaylists(pageQueryDTO));
    }

    @PostMapping("/playlists/{playlistId}")
    @Operation(summary = "收藏歌单")
    public Result collectPlaylist(@PathVariable Long playlistId) {
        userFavoriteService.collectPlaylist(playlistId);
        return Result.success("收藏成功");
    }

    @DeleteMapping("/playlists/{playlistId}")
    @Operation(summary = "取消收藏歌单")
    public Result cancelCollectPlaylist(@PathVariable Long playlistId) {
        userFavoriteService.cancelCollectPlaylist(playlistId);
        return Result.success("取消收藏成功");
    }
}
