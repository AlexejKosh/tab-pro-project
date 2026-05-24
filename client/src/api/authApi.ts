import api from "./apiClient";

import type {
    LoginRequest,
    RegisterRequest,
    RecoverPasswordRequest,
    NewPasswordRequest,
    AuthResponse,
    RecoverPasswordResponse,
    CheckRecoverPasswordTokenResponse
} from "@/types/auth";

export const register = async (data: RegisterRequest): Promise<AuthResponse> => {
    const response =
        await api.post<AuthResponse>(
            "/auth/register",
            data
        );

    return response.data;
};

export const login =
    async (data: LoginRequest): Promise<AuthResponse> => {
    const response =
        await api.post<AuthResponse>(
            "/auth/login",
            data
        );

    return response.data;
};

export const sendRecoverPasswordMail =
    async (data: RecoverPasswordRequest): Promise<RecoverPasswordResponse> => {
    const response =
        await api.post<RecoverPasswordResponse>(
            "/auth/recover-password",
            data
        );

    return response.data;
};

export const checkRecoverPasswordToken =
    async (token: string): Promise<CheckRecoverPasswordTokenResponse> => {
    const response =
        await api.get<CheckRecoverPasswordTokenResponse>(
            `/auth/recover-password/${token}`
        );
        
    return response.data;
};

export const recoverPassword =
async (token: string, data: NewPasswordRequest): Promise<void> => {
    await api.post(
        `/auth/recover-password/${token}`,
        data
    );
};