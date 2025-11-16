package com.mxxdone.miniproject.util;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;

import java.util.regex.Pattern;

public class HtmlSanitizer {

    private static final PolicyFactory POLICY_FACTORY = new HtmlPolicyBuilder()
            .allowStandardUrlProtocols()

            // 1. 기본 텍스트/레이아웃 태그
            .allowElements(
                    "p", "div", "h1", "h2", "h3", "h4", "h5", "h6",
                    "ul", "ol", "li", "blockquote", "br",
                    "b", "i", "u", "strong", "em", "s", "strike",
                    "pre", "code", "span"
            )

            // 2. 이미지 src 허용 (http, https, / 로 시작)
            .allowElements("img")
            .allowAttributes("src")
            .matching(Pattern.compile("^(https?://|/).+$"))
            .onElements("img")

            // 3. 링크 허용
            .allowElements("a")
            .allowAttributes("href")
            .matching(Pattern.compile("^(https?://|mailto:|/).+$"))
            .onElements("a")
            .allowAttributes("target")
            .matching(true, "_blank", "_self")
            .onElements("a")
            .allowAttributes("rel")
            .matching(true, "noopener", "noreferrer")
            .onElements("a")

            // 4. 코드블록 클래스 제한 (pre 태그 추가)
            .allowAttributes("class")
            .matching(Pattern.compile("^language-[a-zA-Z0-9]+$"))
            .onElements("code", "pre") // "pre" 태그를 추가했습니다.

            // 5. 하이라이팅 span 클래스 제한
            .allowAttributes("class")
            .matching(Pattern.compile("^hljs-[a-zA-Z0-9\\-]+$"))
            .onElements("span")

            // 6. Tiptap 유튜브 래퍼(div)의 속성 허용
            .allowAttributes("data-youtube-video")
            .onElements("div")

            // 7. 유튜브 <iframe> 허용
            .allowElements("iframe")
            .allowAttributes("src", "width", "height", "frameborder", "allowfullscreen", "allow")
            // https URL 허용 정규식 수정
            .matching(Pattern.compile("^https://www\\.youtube\\.com/embed/[a-zA-Z0-9_-]+(\\?.*)?$"))
            .onElements("iframe")

            .toFactory();

    public static String sanitize(String rawHtml) {
        if (rawHtml == null) {
            return null;
        }
        return POLICY_FACTORY.sanitize(rawHtml);
    }

    public static String sanitizeTitle(String title) {
        if (title == null) {
            return null;
        }

        String sanitized = title.replaceAll("<[^>]*>", "")
                .replaceAll("(?i)javascript:", "")
                .replaceAll("(?i)on\\w+\\s*=", "");
        return sanitized.trim();
    }
}