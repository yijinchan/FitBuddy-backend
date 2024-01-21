package com.yijinchan.service;

import com.yijinchan.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Random;

@SpringBootTest
public class MockData {
    @Resource
    private UserService userService;
    
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
    private static final String[] GYM_TAGS = {"\"璞瑜\"", "\"意志力\"", "\"集美\"", "\"Z+\"", "\"飞特\"", "\"嘿哈猫\"", "\"巴里\"", "\"校内健身房\""};
    private static final String[] TRAINING_TARGET = {"\"胸部\"", "\"背部\"", "\"肩部\"", "\"手臂\"", "\"腿部\"", "\"臀部\"", "\"有氧\"", "\"功能性\""};
    private static final String[] TRAINING_TiME = {"\"早晨\"", "\"上午\"", "\"中午\"", "\"下午\"", "\"晚上\""};
    private static final String[] FIRST_NAMES = {"Alice", "Bob", "Charlie", "David", "Emily", "Frank", "Grace", "Henry", "Isabella", "Jack", "Kate", "Liam", "Mia", "Nathan", "Olivia", "Peter", "Quinn", "Rachel", "Sarah", "Tyler", "Ursula", "Victoria", "William", "Xander", "Yvonne", "Zachary"};
    private static final String[] LAST_NAMES = {"Anderson", "Brown", "Clark", "Davis", "Evans", "Ford", "Garcia", "Harris", "Isaacs", "Johnson", "Klein", "Lee", "Miller", "Nguyen", "O'Brien", "Parker", "Quinn", "Roberts", "Smith", "Taylor", "Ueda", "Valdez", "Williams", "Xu", "Yamamoto", "Zhang"};
    private static final String[] EMAIL_DOMAINS = {"gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "aol.com", "icloud.com", "protonmail.com", "yandex.com", "mail.com", "inbox.com"};
    private static final String[] GENDER = {"\"男\"", "\"女\"", "\"保密\""};

    @Test
    void insert() {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
//            String randomUsername = getRandomUsername(random);
            String randomUsername = getRandomString(10);
            ArrayList<String> randomTags = getRandomTags(random);
            String randomEmail = randomUsername + "@" + EMAIL_DOMAINS[random.nextInt(EMAIL_DOMAINS.length)];
            String randomProfile = getRandomProfile();
            String randomPhone = getRandomPhone();
            User user = new User();
            user.setId(null);
            user.setUsername(randomUsername);
            user.setPassword("12345678");
            user.setUserAccount(randomUsername.toLowerCase());
            user.setAvatarUrl(avatarUrls[random.nextInt(avatarUrls.length)]);
            user.setEmail(randomEmail);
            user.setProfile(randomProfile);
            user.setPhone(randomPhone);
            user.setTags(randomTags.toString());
            user.setRole(0);
            user.setGender(random.nextInt(2));
            user.setStatus(0);
            user.setIsDelete(0);
            userService.save(user);
        }
    }


    private static String getRandomPhone() {
        Random random = new Random();
        String phoneNumber = "1";
        for (int j = 0; j < 10; j++) {
            phoneNumber += random.nextInt(10);
        }
        return phoneNumber;
    }

    private static String getRandomProfile() {
        Random random = new Random();
        int benchPress = random.nextInt(120) + 120;
        int Squats = random.nextInt(180) + 180;
        int deadLift = random.nextInt(240) + 240;
        int total = benchPress + Squats + deadLift;
        return "我的三大项总成绩为：" + total + "kg，卧推是" + benchPress + "kg，深蹲是" + Squats + "kg，硬拉是" + deadLift +"kg";
    }

    private static String getRandomUsername(Random random) {
        String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
        String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
        return (firstName + lastName);
    }

    private static ArrayList<String> getRandomTags(Random random) {
        ArrayList<String> randomTags = new ArrayList<>();
        randomTags.add(getRandomUniqueValue(GYM_TAGS, random));
        randomTags.add(getRandomUniqueValue(TRAINING_TARGET, random));
        randomTags.add(getRandomUniqueValue(TRAINING_TiME, random));
        String randomGender = getRandomGender(random);
        if (randomGender != null) {
            randomTags.add(randomGender);
        }
        return randomTags;
    }

    private static String getRandomGender(Random random) {
        int randomIndex = random.nextInt(4);
        if (randomIndex == 3) {
            return null;
        } else {
            return GENDER[randomIndex];
        }
    }

    private static ArrayList<String> getRandomValue(String[] values, Random random) {
        ArrayList<String> tagList = new ArrayList<>();
        int randomTagNum = random.nextInt(3) + 1;
        for (int i = 0; i < randomTagNum; i++) {
            tagList.add(values[random.nextInt(values.length)]);
        }
        return tagList;
    }

    private static String getRandomUniqueValue(String[] values, Random random) {
        return values[random.nextInt(values.length)];
    }

    private static String getRandomString(int length) {
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
