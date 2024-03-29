package com.jinchan.utils;

import com.jinchan.common.ErrorCode;
import com.jinchan.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.jinchan.constant.SystemConstants.DEFAULT_BUFFER_SIZE;
import static com.jinchan.constant.SystemConstants.FILE_END;

/**
 * 文件工具
 * @author jinchan
 * @data 2024/2/1
 */
@Component
@Slf4j
public class FileUtils {

    private static String basePath;

    public static String uploadFile(MultipartFile file) {
        //获取原文件名
        String originalFilename = file.getOriginalFilename();
        if (Strings.isEmpty(originalFilename)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //获取后缀名
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        if (Strings.isEmpty(suffix)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        File dir = new File(System.getProperty("user.dir") + basePath);
        //如果文件夹不存在则新建文件夹
        if (!dir.exists()) {
            boolean mkdir = dir.mkdir();
            if (!mkdir) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
        File localFile = new File(System.getProperty("user.dir") + basePath + originalFilename);
        try {
            //将文件从tomcat临时目录转移到指定的目录
            file.transferTo(localFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        byte[] imageStream = getImageStream(localFile);
        String fileName = QiNiuUtils.upload(imageStream);
        boolean delete = localFile.delete();
        if (!delete) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //上传七牛云
        return fileName;
    }

    public static byte[] getImageStream(File imageFile) {
        byte[] buffer = null;
        FileInputStream fis;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            fis = new FileInputStream(imageFile);
            byte[] b = new byte[DEFAULT_BUFFER_SIZE];
            int n;
            while ((n = fis.read(b)) != FILE_END) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (IOException e) {
            log.error("exception message", e);
        }
        return buffer;
    }
    @Value("${fitbuddy.img}")
    public void initBasePath(String b){
        basePath=b;
    }
}
