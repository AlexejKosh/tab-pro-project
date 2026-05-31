import { describe, test, expect } from 'vitest';

import { extractErrorMessage } from '@/utils/error';

describe('extractErrorMessage', () => {
    test('возвращает detail из response.data.detail', () => {
        const error = { response: { data: { detail: 'Подробная ошибка' } } };
        expect(extractErrorMessage(error)).toBe('Подробная ошибка');
    });

    test('парсит JSON в response.data.message и берёт detail', () => {
        const inner = { detail: 'Деталь из JSON' };
        const error = { response: { data: { message: JSON.stringify(inner) } } };
        expect(extractErrorMessage(error)).toBe('Деталь из JSON');
    });

    test('если message не JSON — возвращает сам message', () => {
        const error = { response: { data: { message: 'Простое сообщение' } } };
        expect(extractErrorMessage(error)).toBe('Простое сообщение');
    });

    test('если message — JSON без detail, возвращает исходную строку message', () => {
        const obj = { foo: 'bar' };
        const error = { response: { data: { message: JSON.stringify(obj) } } };
        expect(extractErrorMessage(error)).toBe(JSON.stringify(obj));
    });

    test('если нет response.data, но есть error.message — возвращает его', () => {
        const error = { message: 'Ошибка из message' };
        expect(extractErrorMessage(error)).toBe('Ошибка из message');
    });

    test('приводит числовой detail к строке', () => {
        const error = { response: { data: { detail: 12345 } } };
        expect(extractErrorMessage(error)).toBe('12345');
    });

    test('возвращает defaultMessage если ничего нет', () => {
        expect(extractErrorMessage(undefined)).toBe('Неизвестная ошибка');
        expect(extractErrorMessage(null, 'Кастомная')).toBe('Кастомная');
    });
});
