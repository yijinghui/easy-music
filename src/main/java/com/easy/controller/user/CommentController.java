package com.easy.controller.user;



import com.easy.constant.MessageConstant;
import com.easy.pojo.dto.CommentScrollQueryDTO;
import com.easy.pojo.dto.CommentDTO;
import com.easy.pojo.dto.group.AddGroup;
import com.easy.pojo.dto.group.UpdateGroup;
import com.easy.pojo.vo.CommentInfoVO;
import com.easy.result.Result;
import com.easy.result.ScrollResult;
import com.easy.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/comment")
@RequiredArgsConstructor
@Tag(name = "C端-评论相关接口")
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/song/{songId}/add")
    @Operation(summary = "添加歌曲评论接口")
    public Result addSongComment(
            @PathVariable("songId") Long songId,
            @RequestBody @Validated(AddGroup.class) CommentDTO commentDTO) {
        commentService.add(songId,null, commentDTO);
        return Result.success();
    }


    @PostMapping("/playlist/{playlistId}/add")
    @Operation(summary = "添加歌单评论接口")
    public Result addPlaylistComment(
            @PathVariable("playlistId") Long playlistId,
            @RequestBody @Validated(AddGroup.class) CommentDTO commentDTO) {
        commentService.add(null,playlistId, commentDTO);
        return Result.success();
    }


    @PatchMapping("/like/{commentId}")
    @Operation(summary = "点赞/取消点赞评论接口")
    public Result likeComment(
            @PathVariable("commentId") Long commentId,
            @RequestParam("likeStatus") @NotNull(message = "点赞状态不能为空") Integer likeStatus) {
        commentService.likeComment(commentId, likeStatus);
        return Result.success(likeStatus==1? MessageConstant.LIKE_SUCCESS:MessageConstant.CANCEL_LIKE_SUCCESS);
    }

    /**
     * 删除评论
     * @param commentId 评论id
     * @return 结果
     */
    @DeleteMapping("/{commentId}")
    @Operation(summary = "删除评论接口")
    public Result delete(
            @PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
        return Result.success("删除成功");
    }

    /**
     * 歌曲评论的滚动分页查询
     */
    @GetMapping("/song/{songId}/list")
    @Operation(summary = "歌曲评论的分页查询接口")
    public Result<ScrollResult> listSongComment(
            @PathVariable("songId") Long songId,
            @Valid CommentScrollQueryDTO queryDTO) {
        ScrollResult scrollResult = commentService.list(songId,null, queryDTO);
        return Result.success(scrollResult);
    }

    /**
     * 歌单评论的滚动分页查询
     */

    @GetMapping("/playlist/{playlistId}/list")
    @Operation(summary = "歌单评论的分页查询接口")
    public Result<ScrollResult> listPlaylistComment(
            @PathVariable("playlistId") Long playlistId,
            @Valid CommentScrollQueryDTO queryDTO) {
        ScrollResult scrollResult = commentService.list(null,playlistId, queryDTO);
        return Result.success(scrollResult);
    }

}
