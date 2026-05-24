import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import HomePage from '@/pages/home-page/home-page';
import { useAuthStore } from '@/store/authStore';

describe('HomePage', () => {
    beforeEach(() => {
        useAuthStore.setState({ token: null, user: null, isAuthenticated: false });
        document.title = '';
    });

    test('HomePage - отображает AuthorizedHomeMain при авторизованном пользователе', () => {
        useAuthStore.setState({ isAuthenticated: true });
        render(
            <MemoryRouter>
                <HomePage />
            </MemoryRouter>
        );

        expect(screen.getByRole('button', { name: /Сменить тему/i })).toBeInTheDocument();
        expect(document.title).toBe('Главная страница');
    });

    test('HomePage - отображает UnauthorizedHomeMain при неавторизованном пользователе', () => {
        useAuthStore.setState({ isAuthenticated: false });
        render(
            <MemoryRouter>
                <HomePage />
            </MemoryRouter>
        );

        expect(screen.getByText(/Генерация соло/i)).toBeInTheDocument();
        expect(screen.getByText(/Быстрый старт/i)).toBeInTheDocument();
        expect(document.title).toBe('Главная страница');
    });
});
