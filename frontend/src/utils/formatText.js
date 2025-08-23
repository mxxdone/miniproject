// HTML 문자열에서 HTML 태그를 제거하고 순수 텍스트만 반환
export function stripHtml(html) {
  if (!html) return '';
  // 줄바꿈 역할 태그들을 공백으로 치환
  const normalized = html
    .replace(/<br\s*\/?>/gi, ' ')
    .replace(/<\/p\s*>/gi, ' ')
    .replace(/<\/div\s*>/gi, ' ')
    .replace(/<\/li\s*>/gi, ' ')
    // 여는 블록 태그는 그냥 공백
    .replace(/<(p|div|li)(\s[^>]*)?>/gi, ' ');

  const doc = new DOMParser().parseFromString(normalized, 'text/html');
  // 공백 여러 개를 하나로 줄이고 trim
  return (doc.body.textContent || '')
    .replace(/\s+/g, ' ')
    .trim();
}
