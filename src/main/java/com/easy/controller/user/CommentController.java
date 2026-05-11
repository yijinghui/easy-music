package com.easy.controller.user;



import com.easy.pojo.dto.CommentPlaylistDTO;
import com.easy.pojo.dto.CommentScrollQueryDTO;
import com.easy.pojo.dto.CommentSongDTO;
import com.easy.pojo.vo.CommentVO;
import com.easy.result.Result;
import com.easy.result.ScrollResult;
import com.easy.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("/user/comment")
@RequiredArgsConstructor
@Tag(name = "C端-评论相关接口")
public class CommentController {

    private final CommentService commentService;


    @PostMapping("/addSongComment")
    @Operation(summary = "添加歌曲评论接口")
    public Result addSongComment(@RequestBody CommentSongDTO commentSongDTO) {
        return commentService.addSongComment(commentSongDTO);
    }


    @PostMapping("/addPlaylistComment")
    @Operation(summary = "添加歌单评论接口")
    public Result addPlaylistComment(@RequestBody CommentPlaylistDTO commentPlaylistDTO) {
        return commentService.addPlaylistComment(commentPlaylistDTO);
    }


    @PatchMapping("/likeComment/{id}")
    @Operation(summary = "点赞/取消点赞评论接口")
    public Result likeComment(@PathVariable("id") Long commentId, @RequestParam("likeStatus") Integer likeStatus) {
        return commentService.likeComment(commentId, likeStatus);
    }



    /**
     * 删除评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @DeleteMapping("/deleteComment/{id}")
    @Operation(summary = "删除评论接口")
    public Result deleteComment(@PathVariable("id") Long commentId) {
        return commentService.deleteComment(commentId);
    }

    /**
     * 评论的滚动分页查询
     */

    @PostMapping("/song/list")
    @Operation(summary = "歌曲评论的滚动分页查询接口")
    public Result<ScrollResult<CommentVO>> listSongComment(@RequestBody CommentScrollQueryDTO queryDTO) {
        return commentService.listSongComment(queryDTO);
    }

}
