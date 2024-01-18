package com.yijinchan.service;

import com.yijinchan.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Random;

/**
 * ClassName: MockData
 * Package: com.yijinchan.service
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/18 20:59
 */
@SpringBootTest
public class MockData {
    @Resource
    private UserService userService;

    @Test
    void insert() {
        String[] avatarUrls = {
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
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setUsername("测试" + getRandomString(5));
            user.setPassword("12345678");
            int randomInt = random.nextInt(10);
            user.setUserAccount(getRandomString(10));
            user.setAvatarUrl(avatarUrls[randomInt]);
            user.setRole(0);
            user.setGender(1);
            user.setStatus(0);
            user.setIsDelete(0);
            userService.save(user);
        }
    }

    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
