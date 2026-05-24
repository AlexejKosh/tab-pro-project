export interface RegisterRequest {
    username: string;
    email: string;
    password: string;
}

export interface LoginRequest {
    usernameOrEmail: string;
    password: string;
}

export interface RecoverPasswordRequest {
    email: string;
}

export interface NewPasswordRequest {
    password1: string;
    password2: string;
}

export interface AuthResponse {
    token: string;
}

export interface RecoverPasswordResponse {
    message: string;
}

export interface CheckRecoverPasswordTokenResponse {
    valid: boolean;
}