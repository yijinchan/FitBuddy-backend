package com.yijinchan.config;

import com.yijinchan.common.JacksonObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * ClassName: WebMvcConfig
 * Package: com.yijinchan.config
 * Description:
 *  它们用于在HTTP请求和响应之间转换Java对象与HTTP消息体（如JSON、XML等）。
 *  json的序列化与反序列化
 * @Author yijinchan
 * @Create 2024/1/17 23:05
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    /**
     * 扩展消息转换器
     * @param converters 消息转换器列表
     */
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 创建MappingJackson2HttpMessageConverter对象
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        // 设置objectMapper为新的JacksonObjectMapper对象
        converter.setObjectMapper(new JacksonObjectMapper());
        // 将converter添加到converters列表的首位
        converters.add(0, converter);
    }

}
