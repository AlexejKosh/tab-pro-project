import api from "./apiClient";

import type { User, ChangePasswordRequest } from "@/types/user";

export const getCurrentUser =
    async (): Promise<User> => {
    const response = await api.get<User>("/users/me");
    
    return response.data;
};

export const changePassword =
    async (data: ChangePasswordRequest): Promise<void> => {
    await api.put("/users/password", data);
};

export const deleteCurrentUser =
    async (): Promise<void> => {
    await api.delete("/users/me");
};