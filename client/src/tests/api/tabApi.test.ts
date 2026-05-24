import { describe, test, expect, beforeEach, vi } from 'vitest';

import api from '@/api/apiClient';
import * as tabApi from '@/api/tabApi';

describe('tabApi', () => {
    beforeEach(() => {
        vi.restoreAllMocks();
    });

    test('tabApi – getTabs возвращает список табов', async () => {
        const mockData = [{ id: 1, title: 't' }];
        vi.spyOn(api, 'get').mockResolvedValue({ data: mockData } as any);

        const res = await tabApi.getTabs();

        expect(res).toEqual(mockData);
        expect(api.get).toHaveBeenCalledWith('/tabs');
    });

    test('tabApi – getTabById возвращает таб по id', async () => {
        const mockData = { id: 3, title: 'three' };
        vi.spyOn(api, 'get').mockResolvedValue({ data: mockData } as any);

        const res = await tabApi.getTabById(3);

        expect(res).toEqual(mockData);
        expect(api.get).toHaveBeenCalledWith('/tabs/3');
    });

    test('tabApi – generateTab возвращает результат генерации', async () => {
        const mockData = { id: 4, content: 'gen' };
        vi.spyOn(api, 'post').mockResolvedValue({ data: mockData } as any);

        const res = await tabApi.generateTab({ prompt: 'p' } as any);

        expect(res).toEqual(mockData);
        expect(api.post).toHaveBeenCalledWith('/tabs/generate', expect.any(Object));
    });

    test('tabApi – saveTab вызывает POST', async () => {
        const spy = vi.spyOn(api, 'post').mockResolvedValue({} as any);

        await tabApi.saveTab({ title: 'x' } as any);

        expect(spy).toHaveBeenCalledWith('/tabs', expect.any(Object));
    });

    test('tabApi – deleteTab вызывает DELETE', async () => {
        const spy = vi.spyOn(api, 'delete').mockResolvedValue({} as any);

        await tabApi.deleteTab(5);

        expect(spy).toHaveBeenCalledWith('/tabs/5');
    });
});