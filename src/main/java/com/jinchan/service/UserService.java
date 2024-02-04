package com.jinchan.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jinchan.model.domain.User;
import com.jinchan.model.request.UserRegisterRequest;
import com.jinchan.model.request.UserUpdateRequest;
import com.jinchan.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author jinchan
 * @description 针对表【user】的数据库操作Service
 */
public interface UserService extends IService<User> {
    String userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request);

    String userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User originUser);

    int userLogout(HttpServletRequest request);

    Page<User> searchUsersByTags(List<String> tagNameList, long currentPage);

    boolean isAdmin(User loginUser);

    boolean updateUser(User user, HttpServletRequest request);

    Page<UserVO> userPage(long currentPage);

    User getLoginUser(HttpServletRequest request);


    Boolean isLogin(HttpServletRequest request);

    Page<UserVO> matchUser(long currentPage, User loginUser);

    UserVO getUserById(Long userId, Long loginUserId);

    List<String> getUserTags(Long id);

    void updateTags(List<String> tags, Long userId);

    void updateUserWithCode(UserUpdateRequest updateRequest, Long userId);

    Page<UserVO> getRandomUser();

    void updatePassword(String phone, String code, String password, String confirmPassword);

    Page<UserVO> preMatchUser(long currentPage, String username, User loginUser);

    String afterInsertUser(String key, long userId, HttpServletRequest request);
}
