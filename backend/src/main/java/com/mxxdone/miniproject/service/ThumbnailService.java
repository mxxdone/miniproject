package com.mxxdone.miniproject.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailService {

    private static final String THUMBNAIL_PREFIX = "thumbnails/";
    private static final String THUMBNAIL_SOURCE_PREFIX = "thumbnail-source/";

    private final S3Uploader s3Uploader;
    private final ContentImageService contentImageService;

    /**
     * 게시글 본문 HTML에서 첫 번째 이미지 하나를 찾는다.
     * Lambda에서 비동기 리사이징 처리
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

        // thumbnail-source/ 에 복사 -> S3 이벤트로 Lambda 자동 트리거
        String triggerKey = THUMBNAIL_SOURCE_PREFIX + UUID.randomUUID() + "_thumb.jpg";
        s3Uploader.copy(sourceKey, triggerKey);

        // 썸네일 저장 경로 생성
        // e.g.: thumbnails/랜덤UUID문자열_thumb.jpg
        String thumbKey = triggerKey.replace(THUMBNAIL_SOURCE_PREFIX, THUMBNAIL_PREFIX);
        // 썸네일 byte[]를 S3에 업로드하고 업로드된 썸네일의 CloudFront URL 반환
        return s3Uploader.getUrl(thumbKey);
    }
}