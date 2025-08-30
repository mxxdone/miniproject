package com.mxxdone.miniproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            // .gitignore같은 숨김파일 때문에 추가
            if (dotIndex > 0) {
                // "." 이후 확장자 가져오기
                extension = originalFilename.substring(dotIndex);
            }
        }
        // 날짜 폴더 경로 생성 (yyyy/MM/dd)
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 최종 파일 경로에 날짜 경로 추가 (파일명은 UUID + 확장자)
        String savedFileName = dirName + "/" + datePath + "/" + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(savedFileName)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

        // 업로드된 파일의 URL을 반환
        return s3Client.utilities().getUrl(builder -> builder.bucket(bucket).key(savedFileName)).toString();
    }
}
