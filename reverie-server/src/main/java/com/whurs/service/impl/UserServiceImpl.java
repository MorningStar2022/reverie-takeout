package com.whurs.service.impl;

import com.whurs.constant.MessageConstant;
import com.whurs.dto.UserLoginDTO;
import com.whurs.entity.User;
import com.whurs.exception.LoginFailedException;
import com.whurs.mapper.UserMapper;
import com.whurs.properties.WeChatProperties;
import com.whurs.service.UserService;
import com.whurs.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private static final String WX_LOGIN="https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxlogin(UserLoginDTO userLoginDTO) {
        //调用方法获取用户openid
        String openid = getOpenid(userLoginDTO);
        //判断openid是否为空，为空则登录失败，抛出异常
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //判断当前用户是否为新用户
        User user=userMapper.getByOpenid(openid);
        //如果是新用户，自动完成注册
        if(user==null){
            user= User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回用户对象
        return user;
    }

    private String getOpenid(UserLoginDTO userLoginDTO){
        //调用微信接口服务，获取用户的openid
        Map<String, String> paramMap=new HashMap<>();
        paramMap.put("appid",weChatProperties.getAppid());
        paramMap.put("secret",weChatProperties.getSecret());
        paramMap.put("js_code", userLoginDTO.getCode());
        paramMap.put("grant_type","authorization_code");
        String openid = HttpClientUtil.doGet(WX_LOGIN, paramMap);
        return DigestUtils.md5DigestAsHex(openid.getBytes());
    }
}
