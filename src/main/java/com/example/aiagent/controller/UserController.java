package com.example.aiagent.controller;

import cn.dev33.satoken.util.SaResult;
import com.example.aiagent.entity.dto.RequestLogin;
import com.example.aiagent.entity.dto.RequestRegister;
import com.example.aiagent.entity.vo.LoginUserVO;
import com.example.aiagent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class UserController {


    @Autowired
    private UserService userService;

    /**
     * 用户登录接口
     *
     * @return BaseResponse<LoginUserVO> 返回用户信息和token
     */
    @PostMapping("/login")
    public SaResult login(@RequestBody RequestLogin requestLogin) {
        if (requestLogin == null) {
            return SaResult.error("账号或者密码错误");
        }
        LoginUserVO login = userService.login(requestLogin);
        return SaResult.ok("登录成功");
    }

    /**
     * 用户注册接口
     *
     * @return BaseResponse<String> 返回操作结果
     */
    @PostMapping("/register")
    public SaResult register(@RequestBody  RequestRegister requestRegister) {
        if (requestRegister == null) {
            SaResult.error("输入信息不完整");
        }
        boolean register = userService.register(requestRegister);
        if (!register) {
            return SaResult.error("注册失败");
        }
        return SaResult.ok("注册成功");
    }

}
