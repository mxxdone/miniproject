package com.mxxdone.miniproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CopyObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
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
     * S3에 저장된 파일을 다운로드해서 byte[] 형태로 반환한다.
     * 썸네일 생성 시 원본 이미지를 가져올 때 사용.
     *
     * @param key S3 객체 key
     * @return 파일 데이터(byte[])
     */
    public byte[] download(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<?> response = s3Client.getObject(request)) {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("S3 다운로드 실패: " + key, e);
        }
    }

    /**
     * 서버 메모리 안에 있는 byte[] 데이터를
     * 새로운 파일로 S3에 업로드한다.
     * 서버가 직접 만든 썸네일 이미지(byte[])를 저장할 때 사용.
     *
     * @param data 업로드할 파일 데이터(byte[])
     * @param key 저장할 S3 경로
     * @param contentType 파일 타입 e.g. image/png
     * @return 업로드된 파일의 CloudFront URL
     */
    public String uploadBytes(byte[] data, String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(request, RequestBody.fromBytes(data));
        return "https://" + cloudfrontDomain + "/" + key;
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
}