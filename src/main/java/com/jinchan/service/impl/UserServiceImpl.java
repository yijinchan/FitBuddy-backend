package com.jinchan.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jinchan.common.ErrorCode;
import com.jinchan.exception.BusinessException;
import com.jinchan.mapper.UserMapper;
import com.jinchan.model.domain.Follow;
import com.jinchan.model.domain.User;
import com.jinchan.model.request.UserRegisterRequest;
import com.jinchan.model.request.UserUpdateRequest;
import com.jinchan.model.vo.UserVO;
import com.jinchan.service.FollowService;
import com.jinchan.service.UserService;
import com.jinchan.utils.AlgorithmUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.jinchan.constant.RedisConstants.*;
import static com.jinchan.constant.SystemConstants.*;
import static com.jinchan.constant.UserConstants.*;

/**
 * @author jinchan
 * @description 针对表【user】的数据库操作Service实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final String[] AVATAR_URLS = {
            "https://tse2-mm.cn.bing.net/th/id/OIP-C.qndqxQYjrVB5oqOxxUcNkwHaLH?w=202&h=303&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse2-mm.cn.bing.net/th/id/OIP-C.UUZdwkH1qIpVhJGnsMVG7gHaL5?w=197&h=316&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse1-mm.cn.bing.net/th/id/OIP-C.s49bKLBiOmAgzP7pvTeSWgHaLH?w=202&h=303&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse1-mm.cn.bing.net/th/id/OIP-C.vOM5M-mcgvOzGBCgJ9q-tQHaKh?w=202&h=288&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse3-mm.cn.bing.net/th/id/OIP-C.fj-p2dTyxXeh6htPkR7GlgHaLH?w=202&h=303&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse1-mm.cn.bing.net/th/id/OIP-C.QZczyduQNUPSgodMasUlXgHaJO?w=202&h=251&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse2-mm.cn.bing.net/th/id/OIP-C.FR2JTWQ5bo_XKNXwXwfQPgHaLH?w=202&h=303&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse3-mm.cn.bing.net/th/id/OIP-C.mpyjffkxslgPodE-emib0gHaJQ?w=202&h=253&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse4-mm.cn.bing.net/th/id/OIP-C.sEV-bu7Ps-W7cvjeX-HhsQHaLt?w=198&h=314&c=7&r=0&o=5&dpr=1.1&pid=1.7",
            "https://tse2-mm.cn.bing.net/th/id/OIP-C.LwSuF8aoVeO-THejH3PoOQHaLG?w=202&h=303&c=7&r=0&o=5&dpr=1.1&pid=1.7"};
    @Resource
    private UserMapper userMapper;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private FollowService followService;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "jinchan";

    /**
     * 用户登记
     *
     * @param userRegisterRequest 用户登记要求
     * @param request             要求
     * @return
     */
    @Override
    public String userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String phone = userRegisterRequest.getPhone();
        //String code = userRegisterRequest.getCode();
        String account = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        //checkRegisterRequest(phone, code, account, password, checkPassword);
        checkAccountValid(account);
        checkAccountRepetition(account);
        checkHasRegistered(phone);
        String key = REGISTER_CODE_KEY + phone;
        //checkCode(code, key);
        checkPassword(password, checkPassword);
        long userId = insetUser(phone, account, password);
        return afterInsertUser(key, userId, request);
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户暗语
     * @param request      要求
     * @return
     */
    @Override
    public String userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        validateUserRequest(userAccount, userPassword);
        User userInDatabase = getUserInDatabase(userAccount, userPassword);
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(userInDatabase);
        // 4. 记录用户的登录态
        return setUserLoginState(request, safetyUser);
    }

    /**
     * 设置用户登录状态
     *
     * @param request    要求
     * @param safetyUser 安全用户
     * @return
     */
    public String setUserLoginState(HttpServletRequest request, User safetyUser) {
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        request.getSession().setMaxInactiveInterval(MAXIMUM_LOGIN_IDLE_TIME);
        String token = UUID.randomUUID().toString(true);
        Gson gson = new Gson();
        String userStr = gson.toJson(safetyUser);
        stringRedisTemplate.opsForValue().set(LOGIN_USER_KEY + token, userStr);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, Duration.ofSeconds(MAXIMUM_LOGIN_IDLE_TIME));
        return token;
    }

    /**
     * 验证用户请求
     *
     * @param userAccount  用户账户
     * @param userPassword 用户暗语
     */
    public void validateUserRequest(String userAccount, String userPassword) {
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数错误");
        }
        if (userAccount.length() < MINIMUM_ACCOUNT_LEN) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号非法");
        }
        if (userPassword.length() < MINIMUM_PASSWORD_LEN) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码非法");
        }
        // 账户不能包含特殊字符
        String validPattern = "[^[a-zA-Z][a-zA-Z0-9_]{4,15}$]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号非法");
        }
    }


    /**
     * 取得数据库数据
     *
     * @param userAccount  用户账户
     * @param userPassword 用户暗语
     * @return
     */
    public User getUserInDatabase(String userAccount, String userPassword) {
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount, userAccount);
        User userInDatabase = this.getOne(userLambdaQueryWrapper);
        if (userInDatabase == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在");
        }
        if (!userInDatabase.getPassword().equals(encryptPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        if (!userInDatabase.getStatus().equals(0)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该用户已被封禁");
        }
        return userInDatabase;
    }

    /**
     * 用户脱敏
     *
     * @param originUser 起源用户
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setRole(originUser.getRole());
        safetyUser.setStatus(originUser.getStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setTags(originUser.getTags());
        safetyUser.setProfile(originUser.getProfile());
        return safetyUser;
    }

    /**
     * 用户注销
     *
     * @param request 要求
     * @return int
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        stringRedisTemplate.delete(LOGIN_USER_KEY + token);
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 按标签搜索用户
     * 根据标签搜索用户（内存过滤）
     *
     * @param tagNameList 用户要拥有的标签
     * @param currentPage 当前页码
     * @return
     */
    @Override
    public Page<User> searchUsersByTags(List<String> tagNameList, long currentPage) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagNameList) {
            userLambdaQueryWrapper = userLambdaQueryWrapper
                    .or().like(Strings.isNotEmpty(tagName), User::getTags, tagName);
        }
        return page(new Page<>(currentPage, PAGE_SIZE), userLambdaQueryWrapper);
    }

    /**
     * 是否为管理员
     *
     * @param loginUser 登录用户
     * @return boolean
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getRole() == ADMIN_ROLE;
    }


    /**
     * 使现代化用户
     *
     * @param user    用户
     * @param request 要求
     * @return boolean
     */
    @Override
    public boolean updateUser(User user, HttpServletRequest request) {
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        user.setId(loginUser.getId());
        if (!(isAdmin(loginUser) || loginUser.getId().equals(user.getId()))) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return updateById(user);
    }

    /**
     * 用户分页
     *
     * @param currentPage 当前页码
     * @return
     */
    @Override
    public Page<UserVO> userPage(long currentPage) {
        Page<User> page = this.page(new Page<>(currentPage, PAGE_SIZE));
        Page<UserVO> userVoPage = new Page<>();
        BeanUtils.copyProperties(page, userVoPage);
        return userVoPage;
    }

    /**
     * 收到登录用户
     *
     * @param request 要求
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        String token = request.getHeader("authorization");
        if (StrUtil.isBlank(token)) {
            return null;
        }
        String userStr = stringRedisTemplate.opsForValue().get(LOGIN_USER_KEY + token);
        if (StrUtil.isBlank(userStr)) {
            return null;
        }
        Gson gson = new Gson();
        User user = gson.fromJson(userStr, User.class);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, LOGIN_USER_TTL, TimeUnit.MINUTES);
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        request.getSession().setMaxInactiveInterval(MAXIMUM_LOGIN_IDLE_TIME);
        return user;
    }

    /**
     * 是登录名
     *
     * @param request 要求
     * @return
     */
    @Override
    public Boolean isLogin(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        return userObj != null;
    }

    /**
     * 火柴用户
     *
     * @param currentPage 当前页码
     * @param loginUser   登录用户
     * @return
     */
    @Override
    public Page<UserVO> matchUser(long currentPage, User loginUser) {
        String tags = loginUser.getTags();
        if (tags == null) {
            return this.userPage(currentPage);
        }
        // 获取根据算法排列后的用户列表
        List<Pair<User, Long>> arrangedUser = getArrangedUser(tags, loginUser.getId());
        // 截取currentPage所需的List
        ArrayList<Pair<User, Long>> finalUserPairList = new ArrayList<>();
        int begin = (int) ((currentPage - 1) * PAGE_SIZE);
        int end = (int) (((currentPage - 1) * PAGE_SIZE) + PAGE_SIZE) - 1;
        // 手动整理最后一页
        if (arrangedUser.size() < end) {
            //剩余数量
            int temp = arrangedUser.size() - begin;
            if (temp <= 0) {
                return new Page<>();
            }
            for (int i = begin; i <= begin + temp - 1; i++) {
                finalUserPairList.add(arrangedUser.get(i));
            }
        } else {
            for (int i = begin; i < end; i++) {
                finalUserPairList.add(arrangedUser.get(i));
            }
        }
        // 获取排列后的UserId
        List<Long> userIdList = finalUserPairList.stream().map(pair -> pair.getKey().getId())
                .collect(Collectors.toList());
        List<UserVO> userVOList = getUserListByIdList(userIdList, loginUser.getId());
        Page<UserVO> userVoPage = new Page<>();
        userVoPage.setRecords(userVOList);
        userVoPage.setCurrent(currentPage);
        userVoPage.setSize(userVOList.size());
        userVoPage.setTotal(userVOList.size());
        return userVoPage;
    }

    /**
     * 根据算法排列用户
     *
     * @param tags 标签
     * @param id   id
     * @return
     */
    public List<Pair<User, Long>> getArrangedUser(String tags, Long id) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.select(User::getId, User::getTags);
        List<User> userList = this.list(userLambdaQueryWrapper);
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (User user : userList) {
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), id)) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtil.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        return list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * 通过Id列表获取用户
     *
     * @param userIdList 用户id列表
     * @param userId     用户id
     * @return
     */
    public List<UserVO> getUserListByIdList(List<Long> userIdList, long userId) {
        String idStr = StringUtils.join(userIdList, ",");
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList).last("ORDER BY FIELD(id," + idStr + ")");
        return this.list(userQueryWrapper)
                .stream()
                .map((user) -> followService.getUserFollowInfo(user, userId))
                .collect(Collectors.toList());
    }

    /**
     * 收到用户通过id
     *
     * @param userId      用户id
     * @param loginUserId 登录用户id
     * @return
     */
    @Override
    public UserVO getUserById(Long userId, Long loginUserId) {
        User user = this.getById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
        followLambdaQueryWrapper.eq(Follow::getUserId, loginUserId).eq(Follow::getFollowUserId, userId);
        long count = followService.count(followLambdaQueryWrapper);
        userVO.setIsFollow(count > 0);
        return userVO;
    }

    /**
     * 收到用户标签
     *
     * @param id id
     * @return
     */
    @Override
    public List<String> getUserTags(Long id) {
        User user = this.getById(id);
        String userTags = user.getTags();
        Gson gson = new Gson();
        return gson.fromJson(userTags, new TypeToken<List<String>>() {
        }.getType());
    }

    /**
     * 更新标记
     *
     * @param tags   标签
     * @param userId 用户id
     */
    @Override
    public void updateTags(List<String> tags, Long userId) {
        User user = new User();
        Gson gson = new Gson();
        String tagsJson = gson.toJson(tags);
        user.setId(userId);
        user.setTags(tagsJson);
        this.updateById(user);
    }

    /**
     * 使现代化用户具有密码
     *
     * @param updateRequest 更新请求
     * @param userId        用户id
     */
    @Override
    public void updateUserWithCode(UserUpdateRequest updateRequest, Long userId) {
        String key;
        boolean isPhone = false;
        if (StringUtils.isNotBlank(updateRequest.getPhone())) {
            key = USER_UPDATE_PHONE_KEY + updateRequest.getPhone();
            isPhone = true;
        } else {
            key = USER_UPDATE_EMAIL_KEY + updateRequest.getEmail();
        }
        String correctCode = stringRedisTemplate.opsForValue().get(key);
        if (correctCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先发送验证码");
        }
        if (!correctCode.equals(updateRequest.getCode())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (isPhone) {
            userLambdaQueryWrapper.eq(User::getPhone, updateRequest.getPhone());
            User user = this.getOne(userLambdaQueryWrapper);
            if (user != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号已被绑定");
            }
        } else {
            userLambdaQueryWrapper.eq(User::getEmail, updateRequest.getEmail());
            User user = this.getOne(userLambdaQueryWrapper);
            if (user != null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已被绑定");
            }
        }
        User user = new User();
        BeanUtils.copyProperties(updateRequest, user);
        user.setId(userId);
        this.updateById(user);
    }

    /**
     * 收到随机用户
     *
     * @return
     */
    @Override
    public Page<UserVO> getRandomUser() {
        List<User> randomUser = userMapper.getRandomUser();
        List<UserVO> userVOList = randomUser.stream().map((item) -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(item, userVO);
            return userVO;
        }).collect(Collectors.toList());
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    /**
     * 更新密码
     *
     * @param phone           电话
     * @param code            密码
     * @param password        暗语
     * @param confirmPassword 确认密码
     */
    @Override
    public void updatePassword(String phone, String code, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        String key = USER_FORGET_PASSWORD_KEY + phone;
        String correctCode = stringRedisTemplate.opsForValue().get(key);
        if (correctCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先获取验证码");
        }
        if (!correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone, phone);
        User user = this.getOne(userLambdaQueryWrapper);
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        user.setPassword(encryptPassword);
        this.updateById(user);
        stringRedisTemplate.delete(key);
    }

    /**
     * 通过用户名收到用户分页
     *
     * @param currentPage 当前页码
     * @param username    用户名
     * @param loginUser   登录用户
     * @return
     */
    public Page<UserVO> getUserPageByUsername(long currentPage, String username, User loginUser) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.like(User::getUsername, username);
        Page<User> userPage = this.page(new Page<>(currentPage, PAGE_SIZE), userLambdaQueryWrapper);
        Page<UserVO> userVOPage = new Page<>();
        BeanUtils.copyProperties(userPage, userVOPage);
        List<UserVO> userVOList = userPage.getRecords()
                .stream().map((user) -> this.getUserById(user.getId(), loginUser.getId()))
                .collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    /**
     * 之前火柴用户
     *
     * @param currentPage 当前页码
     * @param username    用户名
     * @param loginUser   登录用户
     * @return
     */
    @Override
    public Page<UserVO> preMatchUser(long currentPage, String username, User loginUser) {
        Gson gson = new Gson();
        // 用户已登录
        if (loginUser != null) {
            String key = USER_RECOMMEND_KEY + loginUser.getId() + ":" + currentPage;
            Page<UserVO> userVOPage;
            if (StringUtils.isNotBlank(username)) { // 填写了用户名,模糊查询
                userVOPage = getUserPageByUsername(currentPage, username, loginUser);
            } else { // 没有填写用户名,正常匹配
                Boolean hasKey = stringRedisTemplate.hasKey(key);
                if (Boolean.TRUE.equals(hasKey)) { // 存在缓存
                    String userVOPageStr = stringRedisTemplate.opsForValue().get(key);
                    userVOPage = gson.fromJson(userVOPageStr, new TypeToken<Page<UserVO>>() {
                    }.getType());
                } else { // 不存在缓存,匹配后加入缓存
                    userVOPage = this.matchUser(currentPage, loginUser);
                    String userVOPageStr = gson.toJson(userVOPage);
                    stringRedisTemplate.opsForValue().set(key, userVOPageStr);
                }
            }
            return userVOPage;
        } else { // 用户未登录
            if (StringUtils.isNotBlank(username)) { // 禁止未登录用户模糊查询
                throw new BusinessException(ErrorCode.NOT_LOGIN);
            }
            long userNum = this.count();
            // 用户量过少,直接列出用户
            if (userNum <= MINIMUM_ENABLE_RANDOM_USER_NUM) {
                Page<User> userPage = this.page(new Page<>(currentPage, PAGE_SIZE));
                List<UserVO> userVOList = userPage.getRecords().stream().map((user) -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }).collect(Collectors.toList());
                Page<UserVO> userVOPage = new Page<>();
                userVOPage.setRecords(userVOList);
                return userVOPage;
            }
            // 用户量足够,随机展示用户
            return this.getRandomUser();
        }
    }

    /**
     * 检查寄存器请求
     *
     * @param phone         电话
     * @param code          密码
     * @param account       账户
     * @param password      暗语
     * @param checkPassword 检查密码
     */
    private void checkRegisterRequest(String phone,
                                      String code,
                                      String account,
                                      String password,
                                      String checkPassword) {
        if (StringUtils.isAnyBlank(phone, code, account, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "信息不全");
        }
        if (StringUtils.isAnyBlank(phone, code, account, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (account.length() < MINIMUM_ACCOUNT_LEN) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (password.length() < MINIMUM_PASSWORD_LEN || checkPassword.length() < MINIMUM_PASSWORD_LEN) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
    }

    /**
     * 支票已注册
     *
     * @param phone 电话
     */
    private void checkHasRegistered(String phone) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone, phone);
        long phoneNum = this.count(userLambdaQueryWrapper);
        if (phoneNum >= 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该手机号已注册");
        }
    }

    /**
     * 校验码
     *
     * @param code 密码
     * @param key  钥匙
     */
    private void checkCode(String code, String key) {
        Boolean hasKey = stringRedisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(hasKey)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "请先获取验证码");
        }
        String correctCode = stringRedisTemplate.opsForValue().get(key);
        if (correctCode == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        if (!correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
    }

    /**
     * 支票帐户有效
     *
     * @param account 账户
     */
    private void checkAccountValid(String account) {
        String validPattern = "[^[a-zA-Z][a-zA-Z0-9_]{4,15}$]";
        Matcher matcher = Pattern.compile(validPattern).matcher(account);
        if (!matcher.find()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号非法");
        }
    }

    /**
     * 检查密码
     *
     * @param password      暗语
     * @param checkPassword 检查密码
     */
    private void checkPassword(String password, String checkPassword) {
        if (!password.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
    }

    /**
     * 支票账户重复
     *
     * @param account 账户
     */
    private void checkAccountRepetition(String account) {
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount, account);
        long count = this.count(userLambdaQueryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
    }

    /**
     * 插入用户
     *
     * @param phone    电话
     * @param account  账户
     * @param password 暗语
     * @return long
     */
    private long insetUser(String phone, String account, String password) {
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        // 3. 插入数据
        User user = new User();
        Random random = new Random();
        user.setAvatarUrl(AVATAR_URLS[random.nextInt(AVATAR_URLS.length)]);
        user.setPhone(phone);
        user.setUsername(account);
        user.setUserAccount(account);
        user.setPassword(encryptPassword);
        ArrayList<String> tag = new ArrayList<>();
        Gson gson = new Gson();
        String jsonTag = gson.toJson(tag);
        user.setTags(jsonTag);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return user.getId();
    }

    /**
     * 之后插入用户
     *
     * @param key     钥匙
     * @param userId  用户id
     * @param request 要求
     * @return
     */
    @Override
    public String afterInsertUser(String key, long userId, HttpServletRequest request) {
        stringRedisTemplate.delete(key);
        User userInDatabase = this.getById(userId);
        User safetyUser = this.getSafetyUser(userInDatabase);
        String token = UUID.randomUUID().toString(true);
        Gson gson = new Gson();
        String userStr = gson.toJson(safetyUser);
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        request.getSession().setMaxInactiveInterval(MAXIMUM_LOGIN_IDLE_TIME);
        stringRedisTemplate.opsForValue().set(LOGIN_USER_KEY + token, userStr);
        stringRedisTemplate.expire(LOGIN_USER_KEY + token, Duration.ofSeconds(MAXIMUM_LOGIN_IDLE_TIME));
        return token;
    }
}

