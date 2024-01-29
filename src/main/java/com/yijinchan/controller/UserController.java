package com.yijinchan.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yijinchan.common.BaseResponse;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.common.ResultUtils;
import com.yijinchan.exception.BusinessException;
import com.yijinchan.model.domain.User;
import com.yijinchan.model.request.UpdatePasswordRequest;
import com.yijinchan.model.request.UserLoginRequest;
import com.yijinchan.model.request.UserRegisterRequest;
import com.yijinchan.model.request.UserUpdateRequest;
import com.yijinchan.model.vo.UserVO;
import com.yijinchan.service.UserService;
import com.yijinchan.utils.SMSUtils;
import com.yijinchan.utils.ValidateCodeUtils;
import io.netty.handler.codec.MessageAggregationException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yijinchan.constant.RedisConstants.*;
import static com.yijinchan.constant.SystemConstants.EMAIL_FROM;
import static com.yijinchan.constant.UserConstants.USER_LOGIN_STATE;


/**
 * 用户管理模块
 *
 * @author jinchan
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Api(tags = "用户管理模块")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private JavaMailSender javaMailSender;  // 注入JavaMailSender对象，用于发送邮件


    /**
     * 短信发送
     *
     * @param phone 手机号
     * @return 发送结果
     */
    @GetMapping("/message")
    @ApiOperation(value = "发送验证码")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "phone", value = "手机号"),
                    @ApiImplicitParam(name = "request", value = "request请求")}
    )
    public BaseResponse<String> sendMessage(String phone, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (StringUtils.isBlank(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer code = ValidateCodeUtils.generateValidateCode(6);
        String key = REGISTER_CODE_KEY + phone;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(code), REGISTER_CODE_TTL, TimeUnit.MINUTES);
//        System.out.println(code);
        SMSUtils.sendMessage(phone, String.valueOf(code));
        return ResultUtils.success("短信发送成功");
    }

    @GetMapping("/message/update/phone")
    @ApiOperation(value = "发送手机号更新验证码")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "phone", value = "手机号")})
    public BaseResponse<String> sendMessage(String phone) {
        if (StringUtils.isBlank(phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer code = ValidateCodeUtils.generateValidateCode(6);
        String key = USER_UPDATE_PHONE_KEY + phone;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(code), USER_UPDATE_PHONE_TTL, TimeUnit.MINUTES);
//        System.out.println(code);
        SMSUtils.sendMessage(phone, String.valueOf(code));
        return ResultUtils.success("短信发送成功");
    }

    @GetMapping("/message/update/email")
    @ApiOperation(value = "发送邮箱更新验证码")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "email", value = "邮箱"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> sendMailUpdateMessage(String email, HttpServletRequest request) throws MessagingException {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer code = ValidateCodeUtils.generateValidateCode(6);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setFrom(new InternetAddress("SUPER <" + EMAIL_FROM + ">"));
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("SUPER 验证码");
        mimeMessageHelper.setText("我们收到了一项请求，要求更新您的邮箱地址为" + email + "。本次操作的验证码为：" + code + "。如果您并未请求此验证码，则可能是他人正在尝试修改以下 SUPER 帐号：" + loginUser.getUserAccount() + "。请勿将此验证码转发给或提供给任何人。");
        javaMailSender.send(mimeMessage);
        String key = USER_UPDATE_EMAIL_KEY + email;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(code), USER_UPDATE_EMAIl_TTL, TimeUnit.MINUTES);
//        System.out.println(code);
        return ResultUtils.success("ok");
    }

    /**
     * 用户注册
     * 接收用户注册请求参数，将用户注册信息保存到数据库
     *
     * @param userRegisterRequest 用户注册请求参数
     * @return 用户注册结果
     */
    @PostMapping("/register")
    @ApiOperation(value = "用户注册")
    @ApiImplicitParams({@ApiImplicitParam(name = "userRegisterRequest", value = "用户注册请求参数")})
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest,HttpServletRequest request) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String phone = userRegisterRequest.getPhone();
        String code = userRegisterRequest.getCode();
        String account = userRegisterRequest.getUserAccount();
        String password = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StringUtils.isAnyBlank(phone, code, account, password, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long userId = userService.userRegister(phone, code, account, password, checkPassword);
        User userInDatabase = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(userInDatabase);
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);
        return ResultUtils.success(userId);
    }

    /**
     * 用户登录
     * 接收用户登录请求参数并返回用户对象
     *
     * @param userLoginRequest 用户登录请求参数
     * @param request          请求对象
     * @return 用户对象或错误信息
     */
    @PostMapping("/login")
    @ApiOperation(value = "用户登录")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "userLoginRequest", value = "用户登录请求参数"),
                    @ApiImplicitParam(name = "request", value = "请求对象")})
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    /**
     * 用户登出
     *
     * @param request 请求对象
     * @return 登出结果
     */
    @PostMapping("/logout")
    @ApiOperation(value = "用户登出")
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "请求对象")})
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        // 检查请求对象是否为空
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 调用用户服务层的用户登出方法
        int result = userService.userLogout(request);
        // 返回成功结果
        return ResultUtils.success(result);
    }
    @GetMapping("/forget")
    @ApiOperation(value = "通过手机号查询用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "phone", value = "手机号")})
    public BaseResponse<String> getUserByPhone(String phone) {
        if (phone==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userLambdaQueryWrapper.eq(User::getPhone, phone);
        User user = userService.getOne(userLambdaQueryWrapper);
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该手机号未绑定账号");
        } else {
            String key = USER_FORGET_PASSWORD_KEY + phone;
            Integer code = ValidateCodeUtils.generateValidateCode(4);
            SMSUtils.sendMessage(phone, String.valueOf(code));
//            System.out.println(code);
            stringRedisTemplate.opsForValue().set(key, String.valueOf(code), USER_FORGET_PASSWORD_TTL, TimeUnit.MINUTES);
            return ResultUtils.success(user.getUserAccount());
        }
    }

    @GetMapping("/check")
    @ApiOperation(value = "校验验证码")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "phone", value = "手机号"),
                    @ApiImplicitParam(name = "code", value = "验证码")})
    public BaseResponse<String> checkCode(String phone, String code) {
        String key = USER_FORGET_PASSWORD_KEY + phone;
        String correctCode = stringRedisTemplate.opsForValue().get(key);
        if (correctCode == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先获取验证码");
        }
        if (!correctCode.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        return ResultUtils.success("ok");
    }

    @PutMapping("/forget")
    @ApiOperation(value = "修改密码")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "updatePasswordRequest", value = "修改密码请求")})
    public BaseResponse<String> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest) {
        String phone = updatePasswordRequest.getPhone();
        String code = updatePasswordRequest.getCode();
        String password = updatePasswordRequest.getPassword();
        String confirmPassword = updatePasswordRequest.getConfirmPassword();
        if (StringUtils.isAnyBlank(phone, code, password, confirmPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        userService.updatePassword(phone, code, password, confirmPassword);
        return ResultUtils.success("ok");
    }
    /**
     * 获取当前用户信息
     *
     * @param request HTTP请求
     * @return 当前用户信息
     */
    @GetMapping("/current")
    @ApiOperation(value = "获取当前用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 通过标签搜索用户
     *
     * @param tagNameList 标签列表
     * @return 搜索结果
     */
    @GetMapping("/search/tags")
    @ApiOperation(value = "通过标签搜索用户")
    @ApiImplicitParams({@ApiImplicitParam(name = "tagNameList", value = "标签列表")})
    public BaseResponse<Page<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList, long currentPage) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Page<User> userList = userService.searchUsersByTags(tagNameList, currentPage);
        return ResultUtils.success(userList);
    }

    /**
     * 通过用户名搜索用户
     *
     * @param username 搜索的用户名
     * @param request  HTTP请求对象
     * @return 搜索结果
     */
    @GetMapping("/search")
    @ApiOperation(value = "通过用户名搜索用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "username", value = "用户名"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<User>> searchUsersByUserName(String username, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> lsit = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(lsit);
    }

    /**
     * 更新用户
     *
     * @param updateRequest updateReques
     * @param request       request请求
     * @return 更新成功返回1，更新失败抛出BusinessException
     */
    @PostMapping("/update")
    @ApiOperation(value = "更新用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "user", value = "用户更新请求参数"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> updateUser(@RequestBody UserUpdateRequest updateRequest, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (updateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (StringUtils.isNotBlank(updateRequest.getEmail()) || StringUtils.isNotBlank(updateRequest.getPhone())) {
            if (StringUtils.isBlank(updateRequest.getCode())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入验证码");
            } else {
                userService.updateUserWithCode(updateRequest, loginUser.getId());
                return ResultUtils.success("ok");
            }
        }
        User user = new User();
        BeanUtils.copyProperties(updateRequest, user);
        boolean flag = userService.updateUser(user, request);
        if (flag) {
            return ResultUtils.success("ok");
        } else {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
    }

    /**
     * 删除用户
     *
     * @param id      用户id
     * @param request request请求
     * @return 删除结果
     */
    @PostMapping("/delete")
    @ApiOperation(value = "删除用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "用户id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 推荐用户
     *
     * @param currentPage 当前页
     * @return 返回用户推荐信息
     */
    @GetMapping("/page")
    @ApiOperation(value = "用户分页")
    @ApiImplicitParams({@ApiImplicitParam(name = "currentPage", value = "当前页")})
    public BaseResponse<Page<UserVO>> userPagination(long currentPage) {
        Page<UserVO> userVOPage = userService.userPage(currentPage);
        return ResultUtils.success(userVOPage);
    }

    /**
     * 获取匹配用户
     *
     * @param currentPage 当前页
     * @param request     请求对象
     * @return 匹配用户的响应结果
     */
    @GetMapping("/match")
    @ApiOperation(value = "获取匹配用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "currentPage", value = "当前页"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<Page<UserVO>> matchUsers(long currentPage, HttpServletRequest request) {
        if (currentPage <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean isLogin = userService.isLogin(request);
        if (isLogin) {
            User user = userService.getLoginUser(request);
            return ResultUtils.success(userService.matchUser(currentPage, user));
        } else {
            return ResultUtils.success(userService.getRandomUser());
        }
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取用户")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "id", value = "用户id"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<UserVO> getUserById(@PathVariable Long id, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        if (id == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.getUserById(id, loginUser.getId());
        return ResultUtils.success(userVO);
    }

    @GetMapping("/tags")
    @ApiOperation(value = "获取当前用户标签")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<List<String>> getUserTags(HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        List<String> userTags = userService.getUserTags(loginUser.getId());
        return ResultUtils.success(userTags);
    }

    @PutMapping("/update/tags")
    @ApiOperation(value = "更新用户标签")
    @ApiImplicitParams(
            {@ApiImplicitParam(name = "tags", value = "标签"),
                    @ApiImplicitParam(name = "request", value = "request请求")})
    public BaseResponse<String> updateUserTags(@RequestBody List<String> tags, HttpServletRequest request) {
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        userService.updateTags(tags, loginUser.getId());
        return ResultUtils.success("ok");
    }
}
