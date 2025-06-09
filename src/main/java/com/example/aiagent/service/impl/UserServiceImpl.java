package com.example.aiagent.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.example.aiagent.entity.User;
import com.example.aiagent.entity.dto.RequestLogin;
import com.example.aiagent.entity.dto.RequestRegister;
import com.example.aiagent.entity.vo.LoginUserVO;
import com.example.aiagent.exception.ErrorCode;
import com.example.aiagent.exception.BusinessException;
import com.example.aiagent.mapper.UserMapper;
import com.example.aiagent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.regex.Pattern;

/**
 * @author hy
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2025-06-09 15:26:57
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    // 邮箱格式正则表达式
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$";

    // 密码格式正则表达式：至少8位，包含数字、字母和特殊字符
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    // 手机号格式正则表达式
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Override
    public LoginUserVO login(RequestLogin requestLogin) {
        String email = requestLogin.getEmail();
        String phone = requestLogin.getPhone();
        String password = requestLogin.getPassword();

        // 校验参数 - 邮箱和手机号至少要有一个
        if (StringUtils.isAllBlank(email, phone) || StringUtils.isBlank(password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "登录信息不完整");
        }

        // 查询用户 - 根据邮箱或手机号
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(email)) {
            queryWrapper.eq("email", email);
        } else if (StringUtils.isNotBlank(phone)) {
            queryWrapper.eq("phone", phone);
        }

        User user = this.getOne(queryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "用户不存在");
        }

        // 验证密码（使用MD5加密对比）
        String encryptPassword = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!user.getUserpassword().equals(encryptPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 生成token（使用sa-token的登录方法）
        StpUtil.login(user.getId());

        // 构造返回结果
        LoginUserVO loginUserVO = new LoginUserVO();
        loginUserVO.setId(user.getId());
        loginUserVO.setUsername(user.getUsername());
        loginUserVO.setEmail(user.getEmail());
        loginUserVO.setPhone(user.getPhone());

        return loginUserVO;
    }

    @Override
    public boolean register(RequestRegister requestRegister) {
        // 校验参数
        if (requestRegister == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户信息不能为空");
        }

        String email = requestRegister.getEmail();
        String phone = requestRegister.getPhone();
        String password = requestRegister.getPassword();
        String requirePassword = requestRegister.getRequirePassword();
        String username = requestRegister.getUsername();

        // 检查必填字段
        if (StringUtils.isAnyBlank(email, phone, password, requirePassword, username)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "注册信息不完整，所有字段都必须填写");
        }

        // 验证邮箱格式
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱格式不正确");
        }

        // 验证手机号格式
        if (!Pattern.matches(PHONE_PATTERN, phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号格式不正确");
        }

        // 验证密码格式
        if (!Pattern.matches(PASSWORD_PATTERN, password)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码必须包含数字、字母和特殊字符，且长度至少8位");
        }

        // 验证密码一致性
        if (!password.equals(requirePassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 检查邮箱是否已存在
        QueryWrapper<User> emailQueryWrapper = new QueryWrapper<>();
        emailQueryWrapper.eq("email", email);
        User existingUserByEmail = this.getOne(emailQueryWrapper);
        if (existingUserByEmail != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被注册");
        }

        // 检查手机号是否已存在
        QueryWrapper<User> phoneQueryWrapper = new QueryWrapper<>();
        phoneQueryWrapper.eq("phone", phone);
        User existingUserByPhone = this.getOne(phoneQueryWrapper);
        if (existingUserByPhone != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setEmail(email);
        user.setPhone(phone);
        user.setUsername(username);
        // 密码加密（使用MD5）
        user.setUserpassword(DigestUtils.md5DigestAsHex(password.getBytes()));
        user.setUserrole("user"); // 默认角色为普通用户
        user.setCreatetime(new Date());
        user.setUpdatetime(new Date());
        user.setIsdelete(0);

        // 插入用户
        boolean result = this.save(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "注册失败");
        }

        return true;
    }
}