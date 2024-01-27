package com.yijinchan.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.constant.UserConstants;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.mapper.UserMapper;
import com.yijinchan.model.domain.Follow;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.UserUpdateRequest;
import com.yijinchan.model.vo.UserVO;
import com.yijinchan.service.FollowService;
import com.yijinchan.service.UserService;
import com.yijinchan.utils.AlgorithmUtil;
import javafx.util.Pair;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.yijinchan.constant.RedisConstants.*;
import static com.yijinchan.constant.SystemConstants.PAGE_SIZE;
import static com.yijinchan.constant.UserConstants.USER_LOGIN_STATE;

/**
 * @author jinchan
 * @description 针对表【user】的数据库操作Service实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private static final String[] avatarUrls = {
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

    @Override
    public long userRegister(String phone, String code, String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(phone, code, userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone, phone);
        long phoneNum = this.count(userLambdaQueryWrapper);
        if (phoneNum >= 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "该手机号已注册");
        }
        String key = REGISTER_CODE_KEY + phone;
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
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_account", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        Random random = new Random();
        user.setAvatarUrl(avatarUrls[random.nextInt(avatarUrls.length)]);
        user.setPhone(phone);
        user.setUsername(userAccount);
        user.setUserAccount(userAccount);
        user.setPassword(encryptPassword);
        ArrayList<String> tag = new ArrayList<>();
        Gson gson = new Gson();
        String jsonTag = gson.toJson(tag);
        user.setTags(jsonTag);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        stringRedisTemplate.delete(key);
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getUserAccount,userAccount);
        User userInDatabase = this.getOne(userLambdaQueryWrapper);
        if (userInDatabase==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        if (!userInDatabase.getPassword().equals(encryptPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码错误");
        }
        // 3. 用户脱敏
        User safetyUser = getSafetyUser(userInDatabase);
        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return safetyUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
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
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户（内存过滤）
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public Page<User> searchUsersByTags(List<String> tagNameList, long currentPage) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        for (String tagName : tagNameList) {
            userLambdaQueryWrapper = userLambdaQueryWrapper.or().like(Strings.isNotEmpty(tagName), User::getTags, tagName);
        }
        return page(new Page<>(currentPage, PAGE_SIZE), userLambdaQueryWrapper);
    }

    /**
     * 判断当前登录用户是否为管理员
     *
     * @param loginUser 当前登录用户
     * @return 如果当前登录用户不为空且角色为管理员，则返回true；否则返回false
     */
    @Override
    public boolean isAdmin(User loginUser) {
        return loginUser != null && loginUser.getRole() == UserConstants.ADMIN_ROLE;
    }


    @Override
    public boolean updateUser(User user, HttpServletRequest request) {
        if (user == null || user.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null || loginUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        user.setId(loginUser.getId());
        if (!(isAdmin(loginUser)) || !Objects.equals(loginUser.getId(), user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return updateById(user);
    }

    /**
     * 推荐用户页面
     *
     * @param currentPage 当前页面数
     * @return 页面对象
     */
    public Page<UserVO> userPage(long currentPage) {
        Page<User> page = this.page(new Page<>(currentPage, PAGE_SIZE));
        Page<UserVO> userVOPage = new Page<>();
        BeanUtils.copyProperties(page, userVOPage);
        return userVOPage;
    }


    /**
     * 获取登录用户对象
     *
     * @param request HTTP请求对象
     * @return 登录用户对象
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    public Boolean isLogin(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            return false;
        }
        return true;
    }

    /**
     * 匹配用户
     *
     * @param loginUser 登录用户
     * @return 匹配到的用户列表
     */
//    @Override
//    public List<User> matchUsers(User loginUser, long num) {
//        // 创建查询条件包装器
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        // 选择id和tags字段
//        queryWrapper.select("id", "tags");
//        // 筛选tags不为空的记录
//        queryWrapper.isNotNull("tags");
//        // 查询用户列表
//        List<User> userList = this.list(queryWrapper);
//
//        // 获取登录用户的tags
//        String tags = loginUser.getTags();
//        // 创建Gson对象
//        Gson gson = new Gson();
//        // 将tags字符串转换为List<String>对象
//        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
//        }.getType());
//
//        // 定义用户列表的下标 => 相似度列表
//        List<Pair<User, Long>> list = new ArrayList<>();
//
//        // 依次计算所有用户和当前用户的相似度
//        for (int i = 0; i < userList.size(); i++) {
//            // 获取当前用户
//            User user = userList.get(i);
//            // 获取当前用户的tags
//            String userTags = user.getTags();
//
//            // 无标签或者为当前用户自己
//            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
//                continue;
//            }
//
//            // 将当前用户的tags字符串转换为List<String>对象
//            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
//            }.getType());
//
//            // 计算分数
//            long distance = AlgorithmUtil.minDistance(tagList, userTagList);
//            // 添加到相似度列表
//            list.add(new Pair<>(user, distance));
//        }
//
//        // 按编辑距离由小到大排序
//        List<Pair<User, Long>> topUserPairList = list.stream()
//                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
//                .limit(num)
//                .collect(Collectors.toList());
//
//        // 获取排序后的用户id列表
//        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
//
//        // 构建in查询的字段列表
//        String idStr = StringUtils.join(userIdList, ",");
//
//        // 创建查询条件包装器，查询匹配的用户
//        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
//        userQueryWrapper.in("id", userIdList).last("ORDER BY FIELD(id,"+idStr+")");
//
//        // 查询并返回匹配到的用户列表
//        return this.list(userQueryWrapper)
//                .stream()
//                .map(this::getSafetyUser)
//                .collect(Collectors.toList());
//    }
//
    @Override
    public Page<UserVO> matchUser(long currentPage, User loginUser) {
        String tags = loginUser.getTags();
        if (tags == null) {
            return this.userPage(currentPage);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || Objects.equals(user.getId(), loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtil.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .collect(Collectors.toList());
        //截取currentPage所需的List
        ArrayList<Pair<User, Long>> finalUserPairList = new ArrayList<>();
        int begin = (int) ((currentPage - 1) * PAGE_SIZE);
        int end = (int) (((currentPage - 1) * PAGE_SIZE) + PAGE_SIZE) - 1;
        if (topUserPairList.size() < end) {
            //剩余数量
            int temp = (int) (topUserPairList.size() - begin);
            if (temp <= 0) {
                return new Page<>();
            }
            for (int i = begin; i <= begin + temp - 1; i++) {
                finalUserPairList.add(topUserPairList.get(i));
            }
        } else {
            for (int i = begin; i < end; i++) {
                finalUserPairList.add(topUserPairList.get(i));
            }
        }
        //获取排列后的UserId
        List<Long> userIdList = finalUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        String idStr = StringUtils.join(userIdList, ",");
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList).last("ORDER BY FIELD(id," + idStr + ")");
        List<UserVO> userVOList = this.list(userQueryWrapper)
                .stream()
                .map((user) -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    LambdaQueryWrapper<Follow> followLambdaQueryWrapper = new LambdaQueryWrapper<>();
                    followLambdaQueryWrapper.eq(Follow::getUserId, loginUser.getId()).eq(Follow::getFollowUserId, userVO.getId());
                    long count = followService.count(followLambdaQueryWrapper);
                    userVO.setIsFollow(count > 0);
                    return userVO;
                })
                .collect(Collectors.toList());
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setRecords(userVOList);
        userVOPage.setCurrent(currentPage);
        userVOPage.setSize(userVOList.size());
        userVOPage.setTotal(userVOList.size());
        return userVOPage;
    }

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
     * 根据内存搜索用户
     *
     * @param tagNameList 标签名列表
     * @return 用户列表
     */
    @Deprecated
    private List<User> searchByMemory(List<String> tagNameList) {
        List<User> userList = userMapper.selectList(null);
        Gson gson = new Gson();
        return userList.stream().filter(user -> {
            String tags = user.getTags();
            Set<String> tempTagNameSet = gson.fromJson(tags, new TypeToken<Set<String>>() {
            }.getType());
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public List<String> getUserTags(Long id) {
        User user = this.getById(id);
        String userTags = user.getTags();
        Gson gson = new Gson();
        return gson.fromJson(userTags, new TypeToken<List<String>>() {
        }.getType());
    }

    @Override
    public void updateTags(List<String> tags, Long userId) {
        User user = new User();
        Gson gson = new Gson();
        String tagsJson = gson.toJson(tags);
        user.setId(userId);
        user.setTags(tagsJson);
        this.updateById(user);
    }

    @Override
    public void updateUserWithCode(UserUpdateRequest updateRequest, Long userId) {
        String key;
        boolean isPhone=false;
        if (StringUtils.isNotBlank(updateRequest.getPhone())) {
            key = USER_UPDATE_PHONE_KEY+updateRequest.getPhone();
            isPhone=true;
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
        if (isPhone){
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getPhone,updateRequest.getPhone());
            User user = this.getOne(userLambdaQueryWrapper);
            if (user!=null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"该手机号已被绑定");
            }
        }else {
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getEmail,updateRequest.getEmail());
            User user = this.getOne(userLambdaQueryWrapper);
            if (user!=null){
                throw new BusinessException(ErrorCode.PARAMS_ERROR,"该邮箱已被绑定");
            }
        }
        User user = new User();
        BeanUtils.copyProperties(updateRequest, user);
        user.setId(userId);
        this.updateById(user);
    }

    @Override
    public Page<UserVO> getRandomUser() {
        List<User> randomUser = userMapper.getRandomUser();
        List<UserVO> userVOList = randomUser.stream().map((item) -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(item, userVO);
            return userVO;
        }).collect(Collectors.toList());
        BeanUtils.copyProperties(randomUser,userVOList);
        Page<UserVO> userVOPage = new Page<>();
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    @Override
    public void updatePassword(String phone, String code, String password, String confirmPassword) {
        if (!password.equals(confirmPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次输入的密码不一致");
        }
        String key = USER_FORGET_PASSWORD_KEY + phone;
        String correctCode = stringRedisTemplate.opsForValue().get(key);
        if (correctCode==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请先获取验证码");
        }
        if (!correctCode.equals(code)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"验证码错误");
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone,phone);
        User user = this.getOne(userLambdaQueryWrapper);
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + password).getBytes());
        user.setPassword(encryptPassword);
        this.updateById(user);
        stringRedisTemplate.delete(key);
    }


}




