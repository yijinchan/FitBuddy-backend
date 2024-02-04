package com.jinchan.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author jinchan
 */
@Data
@ConfigurationProperties(prefix = "fitbuddy.qiniu")
@Component
public class QiNiuProperties {

    private String accessKey;

    private String secretKey;

    private String bucket;

    private String url;
}
