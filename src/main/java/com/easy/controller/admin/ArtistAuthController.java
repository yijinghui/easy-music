package com.easy.controller.admin;

import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.ArtistAuthPageQueryDTO;
import com.easy.pojo.entity.ArtistAuth;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin/auth")
@Tag(name = "Admin端-歌手认证相关接口")
@RequiredArgsConstructor
public class ArtistAuthController {

    private final ArtistAuthService artistAuthService;


    @Operation(summary = "分页查询歌手认证记录")
    @GetMapping
    public Result<PageResult> page(@Valid ArtistAuthPageQueryDTO pageQueryDTO) {
            return Result.success(artistAuthService.page(pageQueryDTO));
    }

    @Operation(summary = "认证记录删除接口")
    @DeleteMapping("/{id}")
    @LogOperation
    public Result<String>delete(@PathVariable Long id) {
        artistAuthService.removeById(id);
        return Result.success("删除成功");
    }

    @Operation(summary = "认证记录编辑/审核接口")
    @PutMapping
    @LogOperation
    public Result audit(@RequestBody @Validated ArtistAuth auth) {
        artistAuthService.audit(auth);
        return Result.success("处理成功");
    }

    @Operation(summary = "认证记录新增接口")
    @PostMapping("/create")
    public Result add(@RequestBody @Validated ArtistAuth auth) {
        artistAuthService.save(auth);
        return Result.success("新增成功");
    }
}
