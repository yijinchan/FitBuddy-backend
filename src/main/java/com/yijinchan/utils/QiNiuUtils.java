package com.yijinchan.utils;

import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.yijinchan.common.ErrorCode;
import com.yijinchan.exception.BusinessException;

import java.io.ByteArrayInputStream;

/**
 * ClassName: QiNiuUtils
 * Package: com.yijinchan.utils
 * Description:
 *
 * @Author yijinchan
 * @Create 2024/1/22 17:24
 */
    public class QiNiuUtils {
        public static void upload(byte[] uploadBytes, String fileName) {
            Configuration cfg = new Configuration(Region.region0());
            UploadManager uploadManager = new UploadManager(cfg);
            //todo 待修改
            String accessKey = "YSE7f7a0FlxYBrsQMHbJj4-B57IdSvPv5a5CNQdP";
            String secretKey = "Y4steLT4wodQWrf5Ji0tOPmqZKzUDiUikCcjo8ya";
            String bucket = "fitbuddy";

            ByteArrayInputStream byteInputStream = new ByteArrayInputStream(uploadBytes);
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response = uploadManager.put(byteInputStream, fileName, upToken, null, null);
            } catch (QiniuException ex) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "头像上传失败");
            }
        }
    }

