package com.easy.controller.user;


import com.easy.pojo.entity.SignIn;
import com.easy.pojo.vo.SignInVO;
import com.easy.result.Result;
import com.easy.service.SignInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/user/signIn")
@RequiredArgsConstructor
@Tag(name = "C端-签到相关接口")
public class SignInController {

    private final SignInService signInService;

    @PostMapping
    @Operation(summary = "用户签到接口")
    public Result signIn() {
        return signInService.signIn();
    }

    @PostMapping("/repair")
    @Operation(summary = "用户补签接口")
    public Result repairSignIn(
            @Parameter(description = "补签目标日期", required = true,example = "2022-01-01")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")LocalDate targetDate) {
        return signInService.repairSignIn(targetDate);
    }


    @GetMapping
    @Operation(summary = "获取当前签到表信息")
    public Result<SignInVO> getSignInInfo(){
        return signInService.getInfo();
    }


}
