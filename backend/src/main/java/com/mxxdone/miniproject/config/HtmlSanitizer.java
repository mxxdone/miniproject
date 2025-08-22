package com.mxxdone.miniproject.config;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;

public class HtmlSanitizer {
    // 기본 정책: 기본적인 텍스트 스타일링(bold, italic 등)만 허용
    private static final PolicyFactory POLICY_FACTORY = Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);

    public static String sanitize(String rawHtml) {
        if (rawHtml == null) {
            return null;
        }
        return POLICY_FACTORY.sanitize(rawHtml);
    }
}