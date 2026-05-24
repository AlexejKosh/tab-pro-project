import axios, { AxiosError } from "axios";

import { getToken, removeToken } from "@/utils/token";
import { useUiStore } from "@/store/uiStore";
import { useAuthStore } from "@/store/authStore";

import type { ApiErrorResponse } from "@/types/api";

const api = axios.create({
    baseURL:
        import.meta.env.VITE_API_BASE_URL,
    headers: {
        "Content-Type":
            "application/json"
    }

});

api.interceptors.request.use(
    (config) => {
        const token = getToken();
        
        if (token) {
            config.headers.Authorization =
                `Bearer ${token}`;
        }

        return config;
    },
    (error) =>
        Promise.reject(error)
);

api.interceptors.response.use(
    (response) => response,
    (error: AxiosError<ApiErrorResponse>) => {
        const status = error.response?.status;

        if (status === 401 && getToken()
        ) {
            removeToken();
            useAuthStore.getState().logout();
            useUiStore
                .getState()
                .setMessage({
                    message:
                        "Сессия истекла. Выполните вход снова.",
                    type:
                        "error"
                });
        }
        else {
            useUiStore
                .getState()
                .setMessage({
                    message:
                        "Выполните вход",
                    type:
                        "error"
                });
        }

        return Promise.reject(error);
    }
);

export default api;