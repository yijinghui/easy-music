package com.easy.controller.admin;


import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.ArtistDTO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/admin/artists")
@Tag(name = "Admin端-歌手管理接口")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    @Operation(summary = "歌手分页查询接口")
    @PostMapping("/page")
    public Result<PageResult> page(@RequestBody ArtistPageQueryDTO pageQueryDTO) {
        return Result.success(artistService.page(pageQueryDTO));
    }

    @Operation(summary = "歌手添加接口")
    @PostMapping("/create")
    @LogOperation
    public Result add(@RequestBody ArtistDTO artistDTO) {
        artistService.add(artistDTO);
        return Result.success("添加成功");
    }

    @Operation(summary = "更新歌手信息接口")
    @PutMapping("/update")
    @LogOperation
    public Result update(@RequestBody ArtistDTO artistDTO) {
        artistService.update(artistDTO);
        return Result.success("更新成功");
    }

    @Operation(summary = "更新歌手头像接口")
    @LogOperation
    @PatchMapping("/avatar/{id}")
    public Result updateAvatar(@PathVariable("id") Long artistId, @RequestParam("avatar") MultipartFile avatar) {
        artistService.updateAvatar(artistId, avatar);
        return Result.success("更新头像成功");
    }

    @Operation(summary = "删除歌手接口")
    @DeleteMapping("/{id}")
    @LogOperation
    public Result delete(@PathVariable("id") Long artistId) {
        artistService.delete(artistId);
        return Result.success("删除成功");
    }


    @Operation(summary = "批量删除歌手接口")
    @DeleteMapping("/batch")
    @LogOperation
    public Result deleteByIds(@RequestBody List<Long> artistIds) {
        artistService.deleteByIds(artistIds);
        return Result.success("删除成功");
    }

    @Operation(summary = "获取所有歌手数量接口")
    @GetMapping("/count")
    public Result<Long> getCount(@RequestParam(required = false) Integer gender, @RequestParam(required = false) String area) {
        return Result.success(artistService.getCount(gender, area));
    }

    @Operation(summary = "获取所有歌手id和名称接口")
    @GetMapping("/names")
    public Result<List<Map<String, Object>>> getNames() {
        return Result.success(artistService.getNames());
    }







}
