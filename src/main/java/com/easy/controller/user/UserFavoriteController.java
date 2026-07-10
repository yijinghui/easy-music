package com.easy.controller.user;

import com.easy.pojo.dto.PageQueryDTO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.UserFavoriteService;
import com.easy.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController("userUserFavoriteController")
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Tag(name = "C端-用户收藏相关接口")
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;

    @GetMapping("/songs")
    @Operation(summary = "获取用户收藏歌曲")
    public Result<PageResult> getUserFavoriteSongs(@Valid PageQueryDTO pageQueryDTO) {
        Long userId = ThreadLocalUtil.getUserId();
        PageResult pageResult = userFavoriteService.getUserFavoriteSongs(userId, pageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/songs/{userId}")
    @Operation(summary = "根据用户ID获取用户收藏歌曲")
    public Result<PageResult> getUserFavoriteSongs(@PathVariable Long userId,@Valid PageQueryDTO pageQueryDTO) {
        PageResult pageResult = userFavoriteService.getUserFavoriteSongs(userId, pageQueryDTO);
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

    @GetMapping("/playlists")
    @Operation(summary = "获取用户收藏歌单")
    public Result<PageResult> getFavoritePlaylists(@Valid PageQueryDTO pageQueryDTO) {
        Long userId = ThreadLocalUtil.getUserId();
        PageResult pageResult = userFavoriteService.getUserFavoritePlaylists(userId, pageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("/playlists/{userId}")
    @Operation(summary = "根据用户ID获取用户收藏歌单")
    public Result<PageResult> getFavoritePlaylists(@PathVariable Long userId,@Valid PageQueryDTO pageQueryDTO) {
        PageResult pageResult = userFavoriteService.getUserFavoritePlaylists(userId, pageQueryDTO);
        return Result.success(pageResult);
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
