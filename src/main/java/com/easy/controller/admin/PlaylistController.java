package com.easy.controller.admin;

import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.*;
import com.easy.pojo.entity.Playlist;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.PlaylistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
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
    public Result<Long> getCount() {
        return Result.success(
                playlistService.getBaseMapper().selectCount(null));
    }


    @Operation(summary = "歌单分页查询接口")
    @PostMapping("/list")
    public Result<PageResult> list(@RequestBody PlaylistPageQueryDTO pageQueryDTO) {
        return Result.success(playlistService.list(pageQueryDTO));
    }

    @Operation(summary = "新增歌单接口")
    @PostMapping("/add")
    @LogOperation
    public Result add(@RequestBody Playlist playlist) {
        playlistService.save(playlist);
        return Result.success("新增成功");
    }

    @Operation(summary = "批量新增歌单歌曲接口")
    @PostMapping("/songs/{playlistId}")
    @LogOperation
    public Result addSongs(@PathVariable Long playlistId, @RequestParam List<Long> songIds) {
        playlistService.addSongs(playlistId,songIds);
        return Result.success("新增成功");
    }



    @Operation(summary = "更新歌单信息接口")
    @PutMapping("/update")
    @LogOperation
    public Result update(@RequestBody Playlist playlist) {
        playlistService.updateById(playlist);
        return Result.success("更新成功");
    }

    @Operation(summary = "更新歌单封面接口")
    @PatchMapping("/cover/{id}")
    @LogOperation
    public Result updateCover(@PathVariable("id") Long playlistId,
                              @RequestParam("cover") @NotBlank(message = "封面不能为空") MultipartFile cover) {
        playlistService.updateCover(playlistId, cover);
        return Result.success("更新成功");
    }


    @Operation(summary = "删除歌单接口")
    @DeleteMapping("/{id}")
    @LogOperation
    public Result delete(@PathVariable("id") Long playlistId) {
        playlistService.delete(playlistId);
        return Result.success("删除成功");
    }

    @Operation(summary = "批量删除歌单歌曲接口")
    @DeleteMapping("/songs/{id}")
    @LogOperation
    public Result deleteSongs(@PathVariable ("id") Long playlistId ,@RequestBody List<Long> songIds) {
        playlistService.deleteSongs(playlistId,songIds);
        return Result.success("批量删除成功");
    }



}
