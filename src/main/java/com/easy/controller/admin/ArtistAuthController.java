package com.easy.controller.admin;

import com.easy.annotation.LogOperation;
import com.easy.pojo.dto.ArtistAuthPageQueryDTO;
import com.easy.pojo.entity.ArtistAuth;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.ArtistAuthService;
import com.minio.MinioTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/admin/auth")
@Tag(name = "Admin端-歌手认证相关接口")
@RequiredArgsConstructor
public class ArtistAuthController {

    private final ArtistAuthService artistAuthService;

    private final MinioTemplate minioTemplate;

    @Operation(summary = "分页查询歌手认证记录")
    @PostMapping("/records")
    public Result<PageResult> getArtistAuth(@RequestBody ArtistAuthPageQueryDTO pageQueryDTO) {
        return artistAuthService.getArtistAuth(pageQueryDTO);
    }

    @Operation(summary = "认证记录删除接口")
    @DeleteMapping("/records/{id}")
    public Result deleteArtistAuth(@PathVariable Long id) {
        return artistAuthService.deleteArtistAuth(id);
    }

    @Operation(summary = "认证记录编辑/审核接口")
    @PutMapping("/records")
    @LogOperation
    public Result updateArtistAuth(@RequestBody ArtistAuth auth) {
        return artistAuthService.updateArtistAuth(auth);
    }

    @Operation(summary = "认证记录新增接口")
    @PostMapping("/records/create")
    public Result addArtistAuth(@RequestBody ArtistAuth auth) {
        return artistAuthService.addArtistAuth(auth);
    }
    @Operation(summary = "营业执照上传接口")
    @PostMapping("/business-license/upload")
    public Result uploadBusinessLicense(@RequestBody MultipartFile businessLicense) {
        String businessLicenseUrl = minioTemplate.uploadFile(businessLicense, "businessLicense");
        return Result.success("上传成功",businessLicenseUrl);
    }




}
