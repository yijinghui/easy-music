package com.easy.controller.user;


import com.easy.pojo.dto.ArtistDTO;
import com.easy.pojo.dto.ArtistPageQueryDTO;
import com.easy.pojo.dto.SongDTO;
import com.easy.pojo.dto.group.UpdateGroup;
import com.easy.pojo.entity.Artist;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistService;
import com.easy.service.SongService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("userArtistController")
@RequestMapping("/artist")
@Tag(name = "C端-歌手相关接口")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final SongService songService;

    @Operation(summary = "根据ID获取歌手详情")
    @GetMapping("/{artistId}")
    public Result<Artist> getById(
            @PathVariable("artistId") Long artistId) {
        return Result.success(artistService.getById(artistId));
    }

    @Operation(summary = "歌手分页查询")
    @PostMapping("/page")
    public Result<PageResult> page(@RequestBody @Valid ArtistPageQueryDTO pageQueryDTO) {
        return Result.success(artistService.page(pageQueryDTO));
    }

    @Operation(summary = "添加歌曲（歌手认证后才能添加）")
    @PostMapping("/add")
    public Result add(@RequestBody @Validated(UpdateGroup.class) SongDTO songDTO) {
        songService.add(songDTO);
        return Result.success("添加成功");
    }


    @Operation(summary = "歌手认证接口")
    @PostMapping("/certify")
    public Result certify(@RequestParam @NotNull(message = "歌手ID不能为空") Long artistId) {
        artistService.certify(artistId);
        return Result.success("认证成功");
    }


}
