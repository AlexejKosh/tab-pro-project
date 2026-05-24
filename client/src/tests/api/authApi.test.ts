import { beforeEach, describe, expect, test, vi } from 'vitest';

import api from '@/api/apiClient';
import * as authApi from '@/api/authApi';

describe('authApi', () => {
    beforeEach(() => {
        vi.restoreAllMocks();
    });

    test('register – возвращает данные ответа', async () => {
        const mockData = {
            token: 't',
            user: {
                id: 1
            }
        };

        vi.spyOn(api, 'post').mockResolvedValue({ data: mockData } as any);
        const res = await authApi.register({
            email: 'a',
            password: 'p',
            name: 'n'
        } as any);

        expect(res).toEqual(mockData);
        expect(api.post).toHaveBeenCalledWith('/auth/register', expect.any(Object));
    });

    test('login – возвращает данные ответа', async () => {
        const mockData = {
            token: 't2',
            user: {
                id: 2
            }
        };

        vi.spyOn(api, 'post').mockResolvedValue({ data: mockData } as any);
        const res = await authApi.login({
            email: 'a',
            password: 'p'
        } as any);

        expect(res).toEqual(mockData);
        expect(api.post).toHaveBeenCalledWith('/auth/login', expect.any(Object));
    });

    test('sendRecoverPasswordMail – возвращает данные ответа', async () => {
        const mockData = {
            ok: true
        };

        vi.spyOn(api, 'post').mockResolvedValue({ data: mockData } as any);
        const res = await authApi.sendRecoverPasswordMail({
            email: 'a'
        } as any);

        expect(res).toEqual(mockData);
        expect(api.post).toHaveBeenCalledWith('/auth/recover-password', expect.any(Object));
    });

    test('checkRecoverPasswordToken – выполняет GET по токену', async () => {
        const mockData = {
            valid: true
        };

        vi.spyOn(api, 'get').mockResolvedValue({ data: mockData } as any);
        const res = await authApi.checkRecoverPasswordToken('tok');

        expect(res).toEqual(mockData);
        expect(api.get).toHaveBeenCalledWith('/auth/recover-password/tok');
    });

    test('recoverPassword – вызывает POST и возвращает void', async () => {
        const spy = vi.spyOn(api, 'post').mockResolvedValue({} as any);
        await authApi.recoverPassword('tok', {
            password: 'p'
        } as any);

        expect(spy).toHaveBeenCalledWith('/auth/recover-password/tok', expect.any(Object));
    });
});