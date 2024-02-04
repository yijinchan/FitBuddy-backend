package com.jinchan.utils;

import com.google.gson.Gson;
import com.jinchan.common.ErrorCode;
import com.jinchan.exception.BusinessException;
import com.jinchan.properties.QiNiuProperties;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;

/**
 * ClassName: 七牛云工具
 * Package: com.jinchan.utils
 * Description:
 *
 * @Author jinchan
 * @Create 2024/1/22 17:24
 */
@Component
public class QiNiuUtils {
    private static QiNiuProperties qiNiuProperties;
    @Resource
    private QiNiuProperties tempProperties;

    public static String upload(byte[] uploadBytes) {
        Configuration cfg = new Configuration(Region.autoRegion());
        UploadManager uploadManager = new UploadManager(cfg);
        //todo 待修改
//        String accessKey = "YSE7f7a0FlxYBrsQMHbJj4-B57IdSvPv5a5CNQdP";
//        String secretKey = "Y4steLT4wodQWrf5Ji0tOPmqZKzUDiUikCcjo8ya";
//        String bucket = "fitbuddy";

        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(uploadBytes);
        Auth auth = Auth.create(qiNiuProperties.getAccessKey(), qiNiuProperties.getSecretKey());
        String upToken = auth.uploadToken(qiNiuProperties.getBucket());
        try {
            Response response = uploadManager.put(byteInputStream, null, upToken, null, null);
            DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
            return putRet.key;
        } catch (QiniuException ex) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
        }
    }

    @PostConstruct
    public void initProperties() {
        qiNiuProperties = tempProperties;
    }
}

