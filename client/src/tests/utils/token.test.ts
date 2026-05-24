import { describe, test, expect, beforeEach } from 'vitest';

import { getToken, setToken, removeToken } from '@/utils/token';

describe('token utils', () => {
    beforeEach(() => {
        localStorage.clear();
    });

    test('token utils - setToken и getToken работают корректно', () => {
        setToken('abc');
        expect(getToken()).toBe('abc');
    });

    test('token utils - removeToken удаляет токен', () => {
        localStorage.setItem('token', 'x');
        removeToken();
        expect(localStorage.getItem('token')).toBeNull();
    });
});
