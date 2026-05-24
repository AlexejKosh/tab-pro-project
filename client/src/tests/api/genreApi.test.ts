import { beforeEach, describe, expect, test, vi } from 'vitest';

import api from '@/api/apiClient';
import * as genreApi from '@/api/genreApi';

describe('genreApi', () => {
    beforeEach(() => {
        vi.restoreAllMocks();
    });

    test('genreApi – getGenres возвращает массив жанров', async () => {
        const mockData = [
            {
                id: 1,
                name: 'rock'
            }
        ];
        vi.spyOn(api, 'get').mockResolvedValue({ data: mockData } as any);
        const res = await genreApi.getGenres();

        expect(res).toEqual(mockData);
        expect(api.get).toHaveBeenCalledWith('/genres');
    });

    test('genreApi – getGenreById возвращает жанр по id', async () => {
        const mockData = {
            id: 2,
            name: 'jazz'
        };
        vi.spyOn(api, 'get').mockResolvedValue({ data: mockData } as any);
        const res = await genreApi.getGenreById(2);

        expect(res).toEqual(mockData);
        expect(api.get).toHaveBeenCalledWith('/genres/2');
    });
});