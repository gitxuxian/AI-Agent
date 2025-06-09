package com.example.aiagent.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.example.aiagent.entity.User;
import com.example.aiagent.entity.dto.RequestLogin;
import com.example.aiagent.entity.dto.RequestRegister;
import com.example.aiagent.entity.vo.LoginUserVO;

/**
 * @author hy
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2025-06-09 15:26:57
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     *
     * @return BaseResponse<LoginUserVO> 返回用户信息和token
     */
    LoginUserVO login(RequestLogin requestLogin);

    /**
     * 用户注册
     *
     * @param user 要注册的用户对象
     * @return BaseResponse<String> 返回操作结果
     */
    boolean register(RequestRegister user);

}
