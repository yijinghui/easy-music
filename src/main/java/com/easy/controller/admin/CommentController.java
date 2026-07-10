package com.easy.controller.admin;


import com.easy.pojo.dto.CommentPageQueryDTO;
import com.easy.result.PageResult;
import com.easy.result.Result;
import com.easy.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Tag(name = "Admin端-评论相关接口")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/list")
    @Operation(summary = "获取所有评论接口")
    public Result<PageResult> pageComments(CommentPageQueryDTO pageQueryDTO){
        return Result.success(commentService.pageComments(pageQueryDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论接口")
    public Result deleteComment(@PathVariable("id") Long commentId){
        commentService.removeById(commentId);
        return Result.success("删除成功");
    }
}
