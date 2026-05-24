import { describe, test, expect, beforeEach, vi } from 'vitest';

import * as tabApi from '@/api/tabApi';
import { useGenerationStore } from '@/store/generationStore';

describe('useGenerationStore', () => {
    beforeEach(() => {
        localStorage.clear();
        useGenerationStore.setState({
            lastRequest: null,
            result: null,
            isGenerating: false,
        });
        vi.restoreAllMocks();
    });

    test('useGenerationStore - startGeneration сохраняет и возвращает результат', async () => {
        const req = { prompt: 'p' } as any;
        const mockRes = {
            id: 1,
            content: 'x',
        } as any;
        vi.spyOn(tabApi, 'generateTab').mockResolvedValue(mockRes);

        const res = await useGenerationStore.getState().startGeneration(req);

        expect(res).toEqual(mockRes);
        expect(useGenerationStore.getState().lastRequest).toEqual(req);
        expect(useGenerationStore.getState().result).toEqual(mockRes);
        expect(useGenerationStore.getState().isGenerating).toBe(false);
    });

    test('useGenerationStore - startGeneration при ошибке сбрасывает isGenerating и пробрасывает ошибку', async () => {
        const req = { prompt: 'p' } as any;
        vi.spyOn(tabApi, 'generateTab').mockRejectedValue(new Error('fail'));

        await expect(useGenerationStore.getState().startGeneration(req)).rejects.toThrow();
        expect(useGenerationStore.getState().isGenerating).toBe(false);
    });

    test('useGenerationStore - setResult и reset работают', () => {
        const r = { id: 2 } as any;
        useGenerationStore.getState().setResult(r);
        expect(useGenerationStore.getState().result).toEqual(r);

        useGenerationStore.getState().reset();
        expect(useGenerationStore.getState().lastRequest).toBeNull();
        expect(useGenerationStore.getState().result).toBeNull();
        expect(useGenerationStore.getState().isGenerating).toBe(false);
    });
});
