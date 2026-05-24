import { describe, test, expect, beforeEach, vi } from 'vitest';

import * as userApi from '@/api/userApi';
import { useAuthStore } from '@/store/authStore';

describe('useAuthStore', () => {
    beforeEach(() => {
        localStorage.clear();
        useAuthStore.setState({
            token: null,
            user: null,
            isAuthenticated: false,
        });
        vi.restoreAllMocks();
    });

    test('useAuthStore - login сохраняет токен и помечает авторизованным', () => {
        useAuthStore.getState().login('tok123');
        expect(localStorage.getItem('token')).toBe('tok123');
        expect(useAuthStore.getState().isAuthenticated).toBe(true);
        expect(useAuthStore.getState().token).toBe('tok123');
    });

    test('useAuthStore - logout очищает токен и состояние', () => {
        localStorage.setItem('token', 'x');
        useAuthStore.setState({ token: 'x', user: { id: 1 } as any, isAuthenticated: true });
        useAuthStore.getState().logout();
        expect(localStorage.getItem('token')).toBeNull();
        expect(useAuthStore.getState().isAuthenticated).toBe(false);
        expect(useAuthStore.getState().user).toBeNull();
    });

    test('useAuthStore - loadCurrentUser устанавливает пользователя при успешном ответе', async () => {
        const mockUser = {
            id: 5,
            name: 'U',
        } as any;
        vi.spyOn(userApi, 'getCurrentUser').mockResolvedValue(mockUser);

        await useAuthStore.getState().loadCurrentUser();
        expect(useAuthStore.getState().user).toEqual(mockUser);
    });

    test('useAuthStore - loadCurrentUser вызывает logout при ошибке', async () => {
        localStorage.setItem('token', 't');
        useAuthStore.setState({ token: 't', isAuthenticated: true });
        vi.spyOn(userApi, 'getCurrentUser').mockRejectedValue(new Error('fail'));

        await useAuthStore.getState().loadCurrentUser();

        expect(useAuthStore.getState().isAuthenticated).toBe(false);
        expect(localStorage.getItem('token')).toBeNull();
    });
});
