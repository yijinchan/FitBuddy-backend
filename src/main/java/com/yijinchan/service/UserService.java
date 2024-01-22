package com.yijinchan.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yijinchan.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author jinchan
 * @description 针对表【user】的数据库操作Service
 */
public interface UserService extends IService<User> {
    long userRegister(String phone, String code, String userAccount, String userPassword, String checkPassword);

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User originUser);

    int userLogout(HttpServletRequest request);

    Page<User> searchUsersByTags(List<String> tagNameList, long currentPage);

    boolean isAdmin(User loginUser);

    boolean updateUser(User user, HttpServletRequest request);

    Page<User> userPage(long currentPage);

    User getLoginUser(HttpServletRequest request);


    Boolean isLogin(HttpServletRequest request);

    Page<User> matchUser(long currentPage, User loginUser);
}
