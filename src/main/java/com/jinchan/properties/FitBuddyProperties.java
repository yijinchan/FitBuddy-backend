package com.jinchan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jinchan
 */
@Data
@ConfigurationProperties(prefix = "fitbuddy")
@Component
public class FitBuddyProperties {

    /**
     * 本地图片位置
     */
    private String img = "/img";

    /**
     * 定时任务
     */
    private String job = "0 0 0 * * ? *";

    /**
     * 启用布隆过滤器
     */
    private boolean enableBloomFilter = false;

    private boolean useShortMessagingService = false;
}
