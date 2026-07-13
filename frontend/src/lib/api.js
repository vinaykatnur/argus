import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_ARGUS_API_BASE_URL || 'http://localhost:8080',
  timeout: 20000,
});

const tokenKeys = [
  'argus-access-token',
  'argus-auth-token',
  'accessToken',
  'token',
];

function readToken() {
  if (typeof window === 'undefined') {
    return null;
  }

  for (const storage of [window.sessionStorage, window.localStorage]) {
    for (const key of tokenKeys) {
      const value = storage.getItem(key);
      if (value) {
        return value;
      }
    }
  }

  return null;
}

api.interceptors.request.use((config) => {
  const token = readToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status !== 401 || originalRequest._retry || originalRequest.url?.includes('/api/v1/auth/')) {
      return Promise.reject(error);
    }

    if (isRefreshing) {
      return new Promise((resolve, reject) => {
        failedQueue.push({ resolve, reject });
      })
        .then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return api(originalRequest);
        })
        .catch((err) => Promise.reject(err));
    }

    originalRequest._retry = true;
    isRefreshing = true;

    const rToken = window.localStorage.getItem('refreshToken');
    if (!rToken) {
      isRefreshing = false;
      // Clear tokens and redirect
      for (const storage of [window.sessionStorage, window.localStorage]) {
        for (const key of [...tokenKeys, 'refreshToken']) {
          storage.removeItem(key);
        }
      }
      if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/signin') && !window.location.pathname.startsWith('/signup') && window.location.pathname !== '/') {
        window.location.href = '/signin';
      }
      return Promise.reject(error);
    }

    try {
      const res = await axios.post(`${import.meta.env.VITE_ARGUS_API_BASE_URL || 'http://localhost:8080'}/api/v1/auth/refresh-token`, {
        refreshToken: rToken,
      });
      const { accessToken, refreshToken: newRefreshToken } = res.data;

      window.localStorage.setItem('argus-access-token', accessToken);
      if (newRefreshToken) {
        window.localStorage.setItem('refreshToken', newRefreshToken);
      }

      originalRequest.headers.Authorization = `Bearer ${accessToken}`;
      processQueue(null, accessToken);
      return api(originalRequest);
    } catch (refreshError) {
      processQueue(refreshError, null);
      for (const storage of [window.sessionStorage, window.localStorage]) {
        for (const key of [...tokenKeys, 'refreshToken']) {
          storage.removeItem(key);
        }
      }
      if (typeof window !== 'undefined' && !window.location.pathname.startsWith('/signin') && !window.location.pathname.startsWith('/signup') && window.location.pathname !== '/') {
        window.location.href = '/signin';
      }
      return Promise.reject(refreshError);
    } finally {
      isRefreshing = false;
    }
  }
);

export function apiErrorMessage(error, fallback = 'Request failed. Please try again.') {
  if (error?.code === 'ECONNABORTED') {
    return 'The request timed out. Please check the backend connection.';
  }
  if (!error?.response) {
    return 'ARGUS backend is unreachable right now.';
  }
  const data = error.response.data;
  if (data?.validationErrors && typeof data.validationErrors === 'object') {
    return Object.entries(data.validationErrors)
      .map(([field, msg]) => `${msg}`)
      .join(', ');
  }
  return data?.message || fallback;
}

export function hasAuthToken() {
  return Boolean(readToken());
}

