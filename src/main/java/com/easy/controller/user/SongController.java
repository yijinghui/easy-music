package com.easy.controller.user;


import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.Song;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController("userSongController")
@RequestMapping("/song")
@RequiredArgsConstructor
@Tag(name = "C端-歌曲相关接口")
public class SongController {

    private final SongService songService;

    @GetMapping("/{id}")
    @Operation(summary = "根据id获取歌曲接口")
    public Result<Song> getById(@PathVariable Long id) {
        Song song = songService.getById(id);
        return Result.success(song);
    }

    @PostMapping("/search")
    @Operation(summary = "搜索歌曲接口")
    public Result<PageResult> search(
            @NotBlank(message = "搜索内容不能为空") String text) throws IOException {
        PageResult pageResult = songService.search(text);
        return Result.success(pageResult);
    }

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐歌曲接口")
    public Result<List<Song>> getRecommendedSongs() {
        List<Song> recommendSongs = songService.getRecommendedSongs();
        return Result.success(recommendSongs);
    }

    /**
     * 获取Top200歌曲
     */
    @GetMapping("/top200/week")
    @Operation(summary = "获取周榜Top200歌曲接口")
    public Result<List<Song>> getTop200SongByWeek(
            @RequestParam @NotNull(message = "查询时间范围不能为空") Integer offset) {
        return Result.success(songService.getTop200SongsByWeek(offset));
    }

    @GetMapping("/top200/month")
    @Operation(summary = "获取月榜Top200歌曲接口")
    public List<Song> getTop200SongByMonth(
            @RequestParam @NotNull(message = "查询时间范围不能为空") Integer offset) {
        return songService.getTop200SongsByMonth(offset);
    }

    @PostMapping("/listen/{songId}")
    @Operation(summary = "用户听歌接口")
    public Result listen(@PathVariable Long songId,
                         @RequestParam(required = false) Long playListId){
        songService.listen(songId,playListId);
        return Result.success();
    }

    @Operation(summary = "获取歌单歌曲内容")
    @GetMapping("/playlist/{playlistId}")
    public Result<PageResult> getSongsByPlaylistId(@PathVariable Long playlistId,PageQueryDTO pageQueryDTO) {
        PageResult pageResult = songService.listSongByPlaylistId(playlistId,pageQueryDTO);
        return Result.success(pageResult);
    }








}
