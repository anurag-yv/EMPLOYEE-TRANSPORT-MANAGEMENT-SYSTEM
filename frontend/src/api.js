import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:1001';

const api = axios.create({
    baseURL: API_BASE_URL,
});

api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        const status = error.response?.status;

        // Intercept expired access token and request refresh
        if (status === 401 && !originalRequest._retry) {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken && !originalRequest.url.includes('/api/auth/refresh') && !originalRequest.url.includes('/api/auth/login')) {
                originalRequest._retry = true;
                try {
                    const res = await axios.post(`${API_BASE_URL}/api/auth/refresh`, { refreshToken });
                    if (res.status === 200) {
                        localStorage.setItem('token', res.data.token);
                        if (res.data.refreshToken) {
                            localStorage.setItem('refreshToken', res.data.refreshToken);
                        }
                        originalRequest.headers.Authorization = `Bearer ${res.data.token}`;
                        return api(originalRequest);
                    }
                } catch (refreshError) {
                    localStorage.removeItem('token');
                    localStorage.removeItem('refreshToken');
                    if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
                        window.location.href = '/login';
                    }
                    return Promise.reject(refreshError);
                }
            }
        }

        if (status === 401 || status === 403) {
            if (!window.location.pathname.includes('/login') && !window.location.pathname.includes('/register')) {
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                window.location.href = '/login';
            }
        }
        return Promise.reject(error);
    }
);

export default api;
