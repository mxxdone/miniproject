package com.mxxdone.miniproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    /**
     * 사용자가 업로드한 원본 이미지를 S3 temp/ 폴더에 임시 저장하고
     * 그 이미지의 CloudFront URL을 반환한다.
     *
     * @param multipartFile 사용자가 업로드한 이미지 파일
     * @return temp/ 에 저장된 이미지의 CloudFront URL
     */
    public String upload(MultipartFile multipartFile) throws IOException {
        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";

        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex > 0) {
                extension = originalFilename.substring(dotIndex);
            }
        }

        // temp/ 경로에 UUID 파일명으로 저장
        String savedFileName = "temp/" + UUID.randomUUID() + extension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(savedFileName)
                .contentType(multipartFile.getContentType())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(multipartFile.getBytes()));

        return "https://" + cloudfrontDomain + "/" + savedFileName;
    }

    /**
     * S3 내부에서 파일을 복사한다.
     * temp/ → images/posts/yyyy/MM/dd/ 형태의 디렉토리로 이동할 때 사용한다.
     *
     * 입력:
     * @param sourceKey 원본 key (e.g.: temp/abc.png)
     * @param destKey   복사 대상 key (e.g.: images/posts/2026/04/04/abc.png)
     */
    public void copy(String sourceKey, String destKey) {
        CopyObjectRequest request = CopyObjectRequest.builder()
                .sourceBucket(bucket)
                .sourceKey(sourceKey)
                .destinationBucket(bucket)
                .destinationKey(destKey)
                .build();

        s3Client.copyObject(request);
    }

    /**
     * S3 key를 CloudFront URL 형태로 바꿔서 반환.
     *
     * @param key S3 객체 key
     * @return CloudFront URL
     */
    public String getUrl(String key) {
        return "https://" + cloudfrontDomain + "/" + key;
    }

    /**
     * S3 객체를 삭제
     *
     * @param key : 삭제할 S3 객체 key
     */
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
    }
}