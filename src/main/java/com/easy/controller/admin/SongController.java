package com.easy.controller.admin;

import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.SongPageQueryDTO;
import com.easy.pojo.entity.Song;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/songs")
@RequiredArgsConstructor
@Tag(name = "Admin端-歌曲管理接口")
public class SongController {

    private final SongService songService;

    @GetMapping("/count")
    @Operation(summary = "获取所有歌曲数量接口")
    public Result<Long> getCount() {
        return Result.success(songService.getBaseMapper().selectCount(null));
    }

    @PostMapping("/list")
    @Operation(summary = "歌曲分页查询接口")
    public Result<PageResult> list(@RequestBody SongPageQueryDTO pageQueryDTO) {
        return Result.success(songService.list(pageQueryDTO));
    }

    @PostMapping("/add")
    @Operation(summary = "添加歌曲接口")
    @LogOperation
    public Result add(@RequestBody Song song) {
        songService.save(song);
        return Result.success("添加成功");
    }

    @PutMapping("/update")
    @Operation(summary = "修改歌曲信息接口")
    @LogOperation
    public Result update(@RequestBody Song song) {
        songService.updateById(song);
        return Result.success("修改成功");
    }

    @PatchMapping("/cover/{id}")
    @Operation(summary = "更新歌曲封面接口")
    @LogOperation
    public Result updateCover(@PathVariable("id") Long songId,
                              @RequestParam("cover") @NotBlank MultipartFile cover) {
        songService.updateCover(songId, cover);
        return Result.success("更新成功");
    }

    @PatchMapping("/audio/{id}")
    @Operation(summary = "更新歌曲音频接口")
    @LogOperation
    public Result updateAudio(
            @PathVariable("id") Long songId,
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("duration") String duration) {
        songService.updateAudio(songId, audio, duration);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除歌曲接口")
    @LogOperation
    public Result deleteById(@PathVariable("id") Long songId) {
        songService.removeById(songId);
        return Result.success("删除成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除歌曲接口")
    @LogOperation
    public Result deleteByIds(@RequestBody List<Long> songIds) {
        songService.removeByIds(songIds);
        return Result.success("删除成功");
    }
}
