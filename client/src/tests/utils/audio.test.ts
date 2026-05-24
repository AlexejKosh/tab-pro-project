import { describe, test, expect } from 'vitest';

import { base64ToAudioUrl } from '@/utils/audio';

describe('base64ToAudioUrl', () => {
    test('base64ToAudioUrl - возвращает data URI, если уже содержит префикс', () => {
        const s = 'data:audio/wav;base64,AAA=';
        expect(base64ToAudioUrl(s)).toBe(s);
    });

    test('base64ToAudioUrl - добавляет префикс, если его нет, и удаляет пробелы', () => {
        const raw = 'A A A=';
        const res = base64ToAudioUrl(raw);
        expect(res.startsWith('data:audio/mpeg;base64,')).toBe(true);
        expect(res).toContain('AAA=');
    });
});
