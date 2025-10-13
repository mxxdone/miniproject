import axios from 'axios';
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

// Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL, // 환경변수를 읽어와서 백엔드 API 서버의 기본 주소 설정
  headers: {
    'Content-Type': 'application/json',
  },
});

// 모든 요청 전에 헤더에 액세스 토큰을 추가하는 '요청 인터셉터'
apiClient.interceptors.request.use(
  (config) => {
    const authStore = useAuthStore();
    const token = authStore.token; // accessToken
    if (token) {
      // 토큰이 있다면, 헤더에 추가
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
)

// 401 에러 발생 시 토큰 재발급을 시도하는 '응답 인터셉터 '
apiClient.interceptors.response.use(
  (response) => {
    // 정상 응답은 그대로 반환
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    const authStore = useAuthStore();

    // 401에러(Unauthorized) & 재시도한 요청이 아닐 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // 무한 재발급 요청 방지

      try {
        console.log("서버로 보내려는 리프레시 토큰:", authStore.refreshToken);
        // apiClient가 아닌 순수 axios를 사용하여 요청 인터셉터를 우회
        const response = await axios.post(`${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/refresh`, {}, {
          headers: { 'Authorization': `Bearer ${authStore.refreshToken}` }
        })

        const { accessToken, refreshToken } = response.data
        authStore.setToken(accessToken, refreshToken)

        // 새로 발급받은 토큰으로 원래 요청의 헤더를 교체
        originalRequest.headers.Authorization = `Bearer ${accessToken}`

        //원래 요청 다시 시도
        return apiClient(originalRequest)

      } catch (refreshError) {
        // 리프레시 토큰 만료된 경우
        console.error("토큰 리프레시 실패:", refreshError);
        alert('세션이 만료되었습니다. 다시 로그인해주세요.')
        authStore.logout();
        await router.push('/login')
        return Promise.reject(refreshError)
      }
    }
    return Promise.reject(error)
  }
)

export default apiClient;
