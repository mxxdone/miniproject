package com.mxxdone.miniproject.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContentImageService {

    private final S3Uploader s3Uploader;

    @Value("${spring.cloud.aws.cloudfront.domain}")
    private String cloudfrontDomain;

    private static final String TEMP_PREFIX = "temp/";
    private static final String POST_IMAGE_PREFIX = "images/posts/";

    /**
     * 게시글 본문 HTML에서 temp/ 경로의 이미지를 찾아
     * 정식 경로(images/posts/yyyy/MM/dd/)로 S3 내부 복사
     * HTML의 src URL도 정식 URL로 교체한 뒤 새 HTML을 반환한다.
     *
     * e.g.
     * 입력 src : https://xxx.cloudfront.net/temp/uuid.png
     * 출력 src : https://xxx.cloudfront.net/images/posts/2025/12/25/uuid.png
     *
     * temp/ 경로가 아닌 이미지는 건드리지 않음
     * (이미 정식 경로에 있는 이미지이거나 외부 이미지인 경우)
     *
     * @param htmlContent 게시글 본문 HTML 문자열
     * @return temp/ URL이 정식 URL로 교체된 HTML 문자열
     */
    public String processTempImages(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) return htmlContent;

        Document doc = Jsoup.parse(htmlContent);
        Elements images = doc.select("img[src]");

        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        for (Element img : images) {
            String src = img.attr("src");

            // CloudFront 도메인 포함하고 temp/ 경로인 경우만 처리
            if (src.contains(cloudfrontDomain) && src.contains("/temp/")) {
                String tempKey = extractS3Key(src);
                String fileName = tempKey.replace(TEMP_PREFIX, "");
                String finalKey = POST_IMAGE_PREFIX + datePath + "/" + fileName;

                // S3 내부 복사
                s3Uploader.copy(tempKey, finalKey);

                // HTML src 교체
                String finalUrl = s3Uploader.getUrl(finalKey);
                img.attr("src", finalUrl);
            }
        }
        return doc.body().html();
    }

    /**
     * 게시글 본문 HTML에서 첫 번째 이미지의 URL을 찾아 반환한다.
     * 이미지가 하나도 없으면 Optional.empty() 반환
     *
     * @param htmlContent 게시글 본문 HTML 문자열
     * @return 첫 번째 이미지 URL을 담은 Optional, 없으면 Optional.empty()
     */
    public Optional<String> extractFirstImageUrl(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) {
            return Optional.empty();
        }

        // HTML 문자열을 파싱해서 문서 객체로 변환
        Document doc = Jsoup.parse(htmlContent);
        // src 속성이 있는 첫 번째 img 태그 선택
        Element firstImg = doc.selectFirst("img[src]");

        if (firstImg == null) {
            return Optional.empty();
        }

        // img 태그의 src 값을 꺼내서 반환
        return Optional.of(firstImg.attr("src"));
    }

    /**
     * CloudFront URL에서 S3 key 부분만 추출한다.
     *
     * e.g.:
     * 입력:
     * https://xxx.cloudfront.net/images/posts/2025/12/25/a.jpg
     *
     * 반환:
     * images/posts/2025/12/25/a.jpg
     *
     * S3에서는 bucket + key 구조로 객체를 관리한다.
     * 여기서 key는 "images/posts/2025/12/25/a.jpg" 같은 전체 경로 문자열이다.
     *
     * @param url CloudFront를 통해 접근하는 전체 이미지 URL
     * @return S3 객체 key
     * @throws RuntimeException CloudFront 도메인으로 시작하지 않는 잘못된 URL인 경우
     */
    public String extractS3Key(String url) {
        String prefix = "https://" + cloudfrontDomain + "/";
        if (!url.startsWith(prefix)) {
            throw new RuntimeException("잘못된 CloudFront URL: " + url);
        }
        return url.substring(prefix.length());
    }
}