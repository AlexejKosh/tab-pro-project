import { create } from "zustand";

import { getToken, setToken, removeToken } from "@/utils/token";
import { getCurrentUser } from "@/api/userApi";

import type { User } from "@/types/user";

interface AuthStore {
    token: string | null;
    user: User | null;
    isAuthenticated: boolean;
    login: (token: string) => void;
    logout: () => void;
    loadCurrentUser: () => Promise<void>;
}

export const useAuthStore = create<AuthStore>((set, get) => ({
    token: getToken(),
    user: null,
    isAuthenticated: !!getToken(),

    login: (token) => {
        setToken(token);
        set({
            token,
            isAuthenticated: true
        });
    },

    logout: () => {
        removeToken();
        set({
            token: null,
            user: null,
            isAuthenticated: false
        });
    },

    loadCurrentUser: async () => {
        try {
            const user = await getCurrentUser();
            set({ user });
        } catch {
            get().logout();
        }
    }
}));