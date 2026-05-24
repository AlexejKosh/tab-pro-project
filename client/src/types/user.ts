export interface User {
    id: number;
    username: string;
    email: string;
    createdAt: string;
}

export interface ChangePasswordRequest {
    oldPassword: string;
    newPassword: string;
}