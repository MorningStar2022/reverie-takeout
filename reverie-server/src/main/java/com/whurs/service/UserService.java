package com.whurs.service;

import com.whurs.dto.UserLoginDTO;
import com.whurs.entity.User;

public interface UserService {
    /**
     * 用户端登录
     * @param userLoginDTO
     * @return
     */
    User wxlogin(UserLoginDTO userLoginDTO);
}
