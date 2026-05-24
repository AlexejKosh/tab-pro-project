import { describe, test, expect, beforeEach, vi } from 'vitest';

import api from '@/api/apiClient';
import { useUiStore } from '@/store/uiStore';
import { useAuthStore } from '@/store/authStore';
import * as token from '@/utils/token';

describe('apiClient', () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    test('apiClient - добавляет Authorization header, если есть токен', () => {
        vi.spyOn(token, 'getToken').mockReturnValue('tok123');
        const cfg: any = { headers: {} };
        const reqHandler = (api as any).interceptors.request.handlers[0].fulfilled;
        const out = reqHandler(cfg);

        expect(out.headers.Authorization).toBe('Bearer tok123');
    });

    test('apiClient - не добавляет Authorization если токена нет', () => {
        vi.spyOn(token, 'getToken').mockReturnValue(null);
        const cfg: any = { headers: {} };
        const reqHandler = (api as any).interceptors.request.handlers[0].fulfilled;
        const out = reqHandler(cfg);

        expect(out.headers.Authorization).toBeUndefined();
    });

    test('apiClient - при 401 и наличии токена выполняет logout, removeToken и показывает сообщение', async () => {
        const mockRemove = vi.spyOn(token, 'removeToken').mockImplementation(() => {});
        vi.spyOn(token, 'getToken').mockReturnValue('t');

        const mockLogout = vi.fn();
        (useAuthStore as any).getState = () => ({ logout: mockLogout });

        const mockSetMessage = vi.fn();
        (useUiStore as any).getState = () => ({ setMessage: mockSetMessage });

        const error: any = { response: { status: 401 } };
        const resHandler = (api as any).interceptors.response.handlers[0].rejected;

        await expect(resHandler(error)).rejects.toEqual(error);

        expect(mockRemove).toHaveBeenCalled();
        expect(mockLogout).toHaveBeenCalled();
        expect(mockSetMessage).toHaveBeenCalledWith({
            message: 'Сессия истекла. Выполните вход снова.',
            type: 'error',
        });
    });

    test('apiClient - при ошибке не-401 показывает общий message', async () => {
        vi.spyOn(token, 'getToken').mockReturnValue(null);

        const mockSetMessage = vi.fn();
        (useUiStore as any).getState = () => ({ setMessage: mockSetMessage });

        const error: any = { response: { status: 500 } };
        const resHandler = (api as any).interceptors.response.handlers[0].rejected;

        await expect(resHandler(error)).rejects.toEqual(error);

        expect(mockSetMessage).toHaveBeenCalledWith({
            message: 'Выполните вход',
            type: 'error',
        });
    });
});
