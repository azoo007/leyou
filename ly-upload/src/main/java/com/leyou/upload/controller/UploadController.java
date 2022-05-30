package com.leyou.upload.controller;

import com.leyou.upload.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
public class UploadController {
    @Autowired
    private UploadService uploadService;

    /**
     * 本地图片上传
     */
    @PostMapping("/image")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file){
        String imageUrl = uploadService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * 获取阿里云签名
     */
    @GetMapping("/signature")
    public ResponseEntity<Map<String,String>> getOssSignature(){
        Map<String,String> resultMap = uploadService.getOssSignature();
        return ResponseEntity.ok(resultMap);
    }
}
