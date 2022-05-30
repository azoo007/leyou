package com.leyou.upload.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.upload.config.OSSProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class UploadService {

    @Value("${ly.upload.imagePath}")
    private String imagePath; //图片本地保存目录
    @Value("${ly.upload.imageUrl}")
    private String imageUrl;//图片的访问路径

    @Autowired
    private OSSProperties ossProps;

    @Autowired
    private OSS ossClient;

    public String uploadImage(MultipartFile file) {
        //判断该文件是否为图片流类型
        // ImageIO: 专门读取图片
        //取出文件流
        try {
            InputStream fileInputStream = file.getInputStream();
            BufferedImage image = ImageIO.read(fileInputStream);
            if(image==null){
                //不是合法图片
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
        }


        //获取文件原名
        String originalFilename = file.getOriginalFilename();
        //获取后缀名
        String extName = originalFilename.substring(originalFilename.lastIndexOf("."));
        //随机文件名称
        String fileName = UUID.randomUUID().toString()+extName;

        //把图片保存到本地目录
        try {
            file.transferTo(new File(imagePath,fileName));

            //返回图片的访问路径
            return imageUrl+fileName;
        } catch (IOException e) {
            e.printStackTrace();
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }

    }

    public Map<String, String> getOssSignature() {

        try {
            long expireTime = ossProps.getExpireTime();
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            // PostObject请求最大可支持的文件大小为5 GB，即CONTENT_LENGTH_RANGE为5*1024*1024*1024。
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, ossProps.getMaxFileSize());
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, ossProps.getDir());

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessId", ossProps.getAccessKeyId());//注意：检查每个key与页面接收的名称是否一致
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", ossProps.getDir());
            respMap.put("host", ossProps.getHost());
            respMap.put("expire", String.valueOf(expireEndTime));//注意：expire超时时间和页面的单位保持一致

            return respMap;
        } catch (Exception e) {
            // Assert.fail(e.getMessage());
            System.out.println(e.getMessage());
            throw new LyException(ExceptionEnum.INVALID_NOTIFY_SIGN);
        } finally {
            ossClient.shutdown();
        }
    }
}
