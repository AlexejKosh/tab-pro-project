import { describe, test, expect } from 'vitest';

import { normalizeChordProgression, isValidChordProgression } from '@/utils/chords';

describe('chords utils', () => {
    test('chords utils - normalizeChordProgression заменяет бемоли на диезы и убирает пробелы', () => {
        const input = 'Db-4, Ebm-3 , C -5';
        const out = normalizeChordProgression(input);
        expect(out).toBe('C#-4,D#m-3,C-5');
    });

    test('chords utils - isValidChordProgression валидирует корректные прогрессии', () => {
        expect(isValidChordProgression('C-4')).toBe(true);
        expect(isValidChordProgression('Am-4, G-4/2')).toBe(true);
    });

    test('chords utils - isValidChordProgression отклоняет некорректные строки', () => {
        expect(isValidChordProgression('invalid')).toBe(false);
        expect(isValidChordProgression('C-')).toBe(false);
    });
});
