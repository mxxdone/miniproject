import axios from 'axios';

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // 백엔드 API 서버의 기본 주소
  headers: {
    'Content-Type': 'application/json',
  },
});

// 요청 인터셉터(interceptor) 설정
apiClient.interceptors.request.use(
  (config) => {
    // localStorage에서 토큰을 가져옵니다.
    const token = localStorage.getItem('jwt');
    if (token) {
      // 토큰이 있다면, 헤더에 추가합니다.
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;
