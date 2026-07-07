package com.easy.controller.admin;


import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.SongDTO;
import com.easy.pojo.dto.SongPageQueryDTO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin/songs")
@Tag(name = "Admin端-歌曲管理接口")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;
    @Operation(summary = "获取所有歌曲数量接口")
    @GetMapping("/count")
    public Result<Long> getAllSongsCount(@RequestParam(required = false) String style) {
        return songService.getAllSongsCount(style);
    }

    @Operation(summary = "歌曲分页查询接口")
    @PostMapping("/page")
    public Result<PageResult> getAllSongs(@RequestBody SongPageQueryDTO pageQueryDTO) {
        return songService.getAllSongs(pageQueryDTO);
    }


    @Operation(summary = "添加歌曲接口")
    @PostMapping("/create")
    @LogOperation
    public Result addSong(@RequestBody SongDTO songDTO) {
        return songService.addSong(songDTO);
    }

    @Operation(summary = "修改歌曲信息接口")
    @PutMapping("/update")
    @LogOperation
    public Result updateSong(@RequestBody SongDTO songDTO) {
        return songService.updateSong(songDTO);
    }


    @Operation(summary = "更新歌曲封面接口")
    @PatchMapping("/cover/{id}")
    @LogOperation
    public Result updateSongCover(@PathVariable("id") Long songId, @RequestParam("cover") MultipartFile cover) {
        return songService.updateSongCover(songId, cover);
    }


    @Operation(summary = "更新歌曲音频接口")
    @PatchMapping("/audio/{id}")
    @LogOperation
    public Result updateSongAudio(@PathVariable("id") Long songId, @RequestParam("audio") MultipartFile audio, @RequestParam("duration") String duration) {
        return songService.updateSongAudio(songId, audio, duration);
    }

    @Operation(summary = "删除歌曲接口")
    @DeleteMapping("/{id}")
    @LogOperation
    public Result deleteSong(@PathVariable("id") Long songId) {
        return songService.deleteSong(songId);
    }

    @Operation(summary = "批量删除歌曲接口")
    @DeleteMapping("/batch")
    @LogOperation
    public Result deleteSongs(@RequestBody List<Long> songIds) {
        return songService.deleteSongs(songIds);
    }





}
