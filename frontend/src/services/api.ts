import axios from 'axios';

export const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
    withCredentials: true,
});

let accessToken: string | null = null;
let isRefreshing = false;
let failedQueue: any[] = [];

const processQueue = (error: any, token: string | null = null) => {
    failedQueue.forEach(prom => {
        if (error) {
            prom.reject(error);
        } else {
            prom.resolve(token);
        }
    });
    failedQueue = [];
};

export const setAccessToken = (token: string | null) => {
    accessToken = token;
};

export const getAccessToken = () => accessToken;

api.interceptors.request.use(
    (config) => {
        if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        
        if (error.response?.status === 401 && !originalRequest._retry) {
            if (originalRequest.url.includes('/api/auth/login') || originalRequest.url.includes('/api/auth/refresh')) {
                return Promise.reject(error);
            }

            if (isRefreshing) {
                return new Promise(function(resolve, reject) {
                    failedQueue.push({resolve, reject});
                }).then(token => {
                    originalRequest.headers['Authorization'] = 'Bearer ' + token;
                    return api(originalRequest);
                }).catch(err => {
                    return Promise.reject(err);
                });
            }

            originalRequest._retry = true;
            isRefreshing = true;

            try {
                const rs = await axios.post(
                    `${api.defaults.baseURL}/api/auth/refresh`,
                    {},
                    { withCredentials: true }
                );
                
                const { accessToken: newAccessToken, user } = rs.data;
                setAccessToken(newAccessToken);
                
                window.dispatchEvent(new CustomEvent('tokenRefreshed', { detail: { user } }));
                
                originalRequest.headers['Authorization'] = `Bearer ${newAccessToken}`;
                
                processQueue(null, newAccessToken);
                
                return api(originalRequest);
            } catch (_error) {
                processQueue(_error, null);
                setAccessToken(null);
                window.dispatchEvent(new Event('logout'));
                return Promise.reject(_error);
            } finally {
                isRefreshing = false;
            }
        }
        
        return Promise.reject(error);
    }
);
