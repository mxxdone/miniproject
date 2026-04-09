package com.mxxdone.miniproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailService {

    // 썸네일 고정 크기
    private static final int THUMB_WIDTH = 300;
    private static final int THUMB_HEIGHT = 200;
    private static final String THUMBNAIL_PREFIX = "thumbnails/";

    private final S3Uploader s3Uploader;
    private final ContentImageService contentImageService;

    /**
     * 게시글 본문 HTML에서 첫 번째 이미지 하나를 찾는다.
     * 해당 이미지를 썸네일로 만들어 S3에 업로드한 썸네일 URL을 반환
     *
     * @param htmlContent 게시글 본문 HTML 문자열
     *                    e.g.: <p>내용</p><img src='https://...cloudfront.net/images/posts/...jpg'>
     * @return 썸네일 URL (이미지 없을시 null)
     */
    public String createThumbnail(String htmlContent) {
        Optional<String> firstImageUrl = contentImageService.extractFirstImageUrl(htmlContent);

        if (firstImageUrl.isEmpty()) {
            return null;
        }

        // CloudFront URL -> S3 key
        // e.g.: https://xxx.cloudfront.net/images/posts/2025/12/25/a.jpg
        //  -> images/posts/2025/12/25/a.jpg
        String sourceKey = contentImageService.extractS3Key(firstImageUrl.get());

        // 원본 다운로드 후 리사이즈
        byte[] originalBytes = s3Uploader.download(sourceKey);
        byte[] thumbnailBytes = resize(originalBytes);

        // 썸네일 저장 경로 생성
        // e.g.: thumbnails/랜덤UUID문자열_thumb.jpg
        String thumbKey =THUMBNAIL_PREFIX + UUID.randomUUID() + "_thumb.jpg";
        // 썸네일 byte[]를 S3에 업로드하고 업로드된 썸네일의 CloudFront URL 반환
        return s3Uploader.uploadBytes(thumbnailBytes, thumbKey, "image/jpeg");
    }

    /**
     *
     * @param originalBytes 원본 이미지 파일 데이터
     * @return 썸네일 이미지 데이터(byte[])
     */
    private byte[] resize(byte[] originalBytes) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(originalBytes);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Thumbnailator 라이브러리 사용
            Thumbnails.of(in)
                    .size(THUMB_WIDTH, THUMB_HEIGHT)
                    .crop(Positions.CENTER) // 중앙 기준 자르기
                    .outputFormat("jpg") //
                    .outputQuality(0.8) // 80% 퀄리티
                    .toOutputStream(out);

            // 메모리에 만들어진 썸네일 데이터를 byte[]로 반환
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("썸네일 생성 실패", e);
        }
    }
}