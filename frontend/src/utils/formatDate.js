import { format, parseISO } from 'date-fns';

export function formatDateTime(isoString) {
  // isoString이 null이거나 undefined일 경우 빈 문자열 반환
  if (!isoString) return '';

  // 백엔드에서 받은 ISO 형식의 문자열을 Date 객체로 파싱
  const date = parseISO(isoString);

  // 원하는 형식으로 변환하여 반환
  return format(date, 'yyyy.MM.dd HH:mm');
}
