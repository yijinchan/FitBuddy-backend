package com.jinchan.aop;

import cn.hutool.bloomfilter.BloomFilter;
import com.jinchan.common.ErrorCode;
import com.jinchan.exception.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.jinchan.constant.BloomFilterConstants.*;

/**
 * @author jinchan
 */
@Component
@Aspect
@ConditionalOnProperty(prefix = "fitbuddy", name = "enable-bloom-filter", havingValue = "true")
@Log4j2
public class BloomFilterSearchAdvice {
    @Resource
    private BloomFilter bloomFilter;


    @Before("execution(* com.jinchan.controller.UserController.getUserById(..))")
    public void findUserById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        boolean contains = bloomFilter.contains(USER_BLOOM_PREFIX + args[0]);
        if (!contains) {
            log.error("没有在 BloomFilter 中找到该 userId");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有找到该用户");
        }
    }

    @Before("execution(* com.jinchan.controller.TeamController.getTeamById(..))")
    public void findTeamById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        boolean contains = bloomFilter.contains(TEAM_BLOOM_PREFIX + args[0]);
        if (!contains) {
            log.error("没有在 BloomFilter 中找到该 teamId");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有找到该队伍");
        }
    }

    @Before("execution(* com.jinchan.controller.TeamController.getTeamMemberById(..))")
    public void findTeamMemberById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        boolean contains = bloomFilter.contains(TEAM_BLOOM_PREFIX + args[0]);
        if (!contains) {
            log.error("没有在 BloomFilter 中找到该 teamId");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有找到该队伍");
        }
    }

    @Before("execution(* com.jinchan.controller.BlogController.getBlogById(..))")
    public void findBlogById(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        boolean contains = bloomFilter.contains(BLOG_BLOOM_PREFIX + args[0]);
        if (!contains) {
            log.error("没有在 BloomFilter 中找到该 blogId");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有找到该博文");
        }
    }
}
