package com.whurs.controller.user;

import com.whurs.constant.JwtClaimsConstant;
import com.whurs.dto.UserLoginDTO;
import com.whurs.entity.User;
import com.whurs.properties.JwtProperties;
import com.whurs.result.Result;
import com.whurs.service.UserService;
import com.whurs.utils.JwtUtil;
import com.whurs.vo.UserLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user/user")
@Api(tags = "用户端登录接口")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("用户端登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO){
        log.info("用户登录，返回code:{}",userLoginDTO);
        UserLoginVO userLoginVO=new UserLoginVO();
        //调用微信登录接口
        User user = userService.wxlogin(userLoginDTO);
        //登录成功也要生成jwt令牌
        Map<String, Object> claims=new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID,user.getId());
        String token = JwtUtil.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), claims);
        userLoginVO.setId(user.getId());
        userLoginVO.setOpenid(user.getOpenid());
        userLoginVO.setToken(token);
        return Result.success(userLoginVO);
    }
}
