import { beforeEach, describe, expect, test, vi } from 'vitest';

import api from '@/api/apiClient';
import * as userApi from '@/api/userApi';

describe('userApi', () => {
    beforeEach(() => {
        vi.restoreAllMocks();
    });

    test('userApi – getCurrentUser возвращает пользователя', async () => {
        const mockData = {
            id: 1,
            name: 'U'
        };

        vi.spyOn(api, 'get').mockResolvedValue({ data: mockData } as any);

        const res = await userApi.getCurrentUser();

        expect(res).toEqual(mockData);
        expect(api.get).toHaveBeenCalledWith('/users/me');
    });

    test('userApi – changePassword вызывает PUT', async () => {
        const spy = vi.spyOn(api, 'put').mockResolvedValue({} as any);

        await userApi.changePassword({
            oldPassword: 'a',
            newPassword: 'b'
        } as any);

        expect(spy).toHaveBeenCalledWith('/users/password', expect.any(Object));
    });

    test('userApi – deleteCurrentUser вызывает DELETE', async () => {
        const spy = vi.spyOn(api, 'delete').mockResolvedValue({} as any);

        await userApi.deleteCurrentUser();

        expect(spy).toHaveBeenCalledWith('/users/me');
    });
});