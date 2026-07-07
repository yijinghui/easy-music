package com.easy.controller.user;

import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.dto.PlaylistDTO;
import com.easy.pojo.dto.group.AddGroup;
import com.easy.pojo.dto.group.UpdateGroup;
import com.easy.pojo.entity.Playlist;
import com.easy.pojo.vo.PlaylistVO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.PlaylistService;
import com.easy.service.SongService;
import com.easy.service.UserService;
import com.easy.utils.ThreadLocalUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController("userPlaylistController")
@RequestMapping("/playlist")
@RequiredArgsConstructor
@Tag(name = "C端-歌单相关接口")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Operation(summary = "搜索歌单接口")
    @GetMapping("/search")
    public Result<PageResult> search (
            @RequestParam("text") @NotBlank(message = "搜索内容不能为空") String text) throws IOException {
        return Result.success(playlistService.search(text));
    }

    @Operation(summary = "获取歌单详情接口")
    @GetMapping("/{playlistId}")
    public Result<PlaylistVO> detail(
            @PathVariable("playlistId") Long playlistId) {
        return Result.success(playlistService.getDetailById(playlistId));
    }


    @Operation(summary = "获取当前用户歌单列表接口")
    @GetMapping("/list")
    public Result<List<Playlist>> list() {
        List<Playlist> playlists = playlistService.listByUserId(ThreadLocalUtil.getUserId());
        return Result.success(playlists);
    }

    @Operation(summary = "获取用户歌单列表接口")
    @GetMapping("/list/{userId}")
    public Result<List<Playlist>> listByUserId(
            @PathVariable Long userId) {
        List<Playlist> playlists = playlistService.listByUserId(userId);
        return Result.success(playlists);
    }

    @Operation(summary = "创建歌单接口")
    @PostMapping("/create")
    public Result create(
            @RequestBody @Validated(AddGroup.class) PlaylistDTO playlistDTO) {
        playlistService.create(playlistDTO);
        return Result.success("创建歌单成功");
    }

    @Operation(summary = "删除歌单接口")
    @DeleteMapping("/{playlistId}")
    public Result delete(
            @PathVariable("playlistId") Long playlistId) {
        playlistService.delete(playlistId);
        return Result.success("删除歌单成功");
    }

    @Operation(summary = "更新歌单信息接口")
    @PutMapping("/update")
    public Result update(
            @RequestBody @Validated(UpdateGroup.class) PlaylistDTO playlistDTO) {
        playlistService.update(playlistDTO);
        return Result.success("更新歌单成功");
    }

    @Operation(summary = "向歌单添加歌曲接口")
    @PostMapping("/{playlistId}/song/{songId}")
    public Result addSong(
            @PathVariable("playlistId") Long playlistId,
            @PathVariable("songId") Long songId) {
        playlistService.addSong(playlistId, songId);
        return Result.success("添加歌曲成功");
    }

    @Operation(summary = "从歌单删除歌曲接口")
    @DeleteMapping("/{playlistId}/song/{songId}")
    public Result removeSong(
            @PathVariable("playlistId") Long playlistId,
            @PathVariable("songId") Long songId) {
        playlistService.removeSong(playlistId, songId);
        return Result.success("删除歌曲成功");
    }
}
