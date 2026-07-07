package com.easy.controller.user;


import com.easy.pojo.dto.PageQueryDTO;
import com.easy.pojo.entity.Song;
import com.easy.result.Result;
import com.easy.service.PlayRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/record")
@RestController
@RequiredArgsConstructor
@Tag(name = "C端-播放记录相关接口")
public class PlayRecordController {
    private final PlayRecordService playRecordService;


    @GetMapping("/list")
    @Operation(summary = "获取用户播放记录接口")
    public Result<List<Song>> list() {
        return Result.success(playRecordService.listByUserId());
    }

    @DeleteMapping("/delete/{songId}")
    @Operation(summary = "删除用户播放记录接口")
    public Result delete(@PathVariable Long songId){
        playRecordService.delete(songId);
        return Result.success();
    }

}
