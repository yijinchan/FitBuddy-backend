package com.FitBuddy.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.FitBuddy.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author jinchan
* @description 针对表【user】的数据库操作Service
*/
public interface UserService extends IService<User> {
    long userRegister(String userAccount, String userPassword, String checkPassword);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User originUser);

    int userLogout(HttpServletRequest request);

    List<User> searchUsersByTags(List<String> tagNameList);

    boolean isAdmin(HttpServletRequest request);
}
