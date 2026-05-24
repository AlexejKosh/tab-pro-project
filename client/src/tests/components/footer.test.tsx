import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import Footer from '@/components/common/footer/footer';

import { useAuthStore } from '@/store/authStore';

describe('Footer', () => {
    beforeEach(() => {
        useAuthStore.setState({ isAuthenticated: false });
    });

    test('Footer - показывает ссылки для гостя на главной странице', () => {
        render(
            <MemoryRouter initialEntries={['/']}>
                <Footer />
            </MemoryRouter>
        );

        expect(screen.getByText('Регистрация')).toBeInTheDocument();
    });

    test('Footer - показывает ссылку на профиль для авторизованного пользователя', () => {
        useAuthStore.setState({ isAuthenticated: true });

        render(
            <MemoryRouter initialEntries={['/']}>
                <Footer />
            </MemoryRouter>
        );

        expect(screen.getByText('Профиль')).toBeInTheDocument();
    });

    test('Footer - показывает копирайт', () => {
        render(
            <MemoryRouter>
                <Footer />
            </MemoryRouter>
        );

        expect(screen.getByText(/Кошелев Алексей/i)).toBeInTheDocument();
    });

    test('Footer - показывает ссылки на странице about для гостя', () => {
        useAuthStore.setState({ isAuthenticated: false });

        render(
            <MemoryRouter initialEntries={['/about']}>
                <Footer />
            </MemoryRouter>
        );

        expect(screen.getByText('Главная страница')).toBeInTheDocument();
        expect(screen.getByText('Регистрация')).toBeInTheDocument();
        expect(screen.getByText('Генерация табулатуры')).toBeInTheDocument();
    });

    test('Footer - показывает ссылки на странице generate для авторизованного пользователя', () => {
        useAuthStore.setState({ isAuthenticated: true });

        render(
            <MemoryRouter initialEntries={['/generate']}>
                <Footer />
            </MemoryRouter>
        );

        expect(screen.getByText('Главная страница')).toBeInTheDocument();
        expect(screen.getByText('Профиль')).toBeInTheDocument();
        expect(screen.getByText('О проекте')).toBeInTheDocument();
    });

    test('Footer - показывает ссылки на вкладках', () => {
        useAuthStore.setState({ isAuthenticated: true });

        render(
            <MemoryRouter initialEntries={['/tabs/1']}>
                <Footer />
            </MemoryRouter>
        );

        expect(screen.getByText('Главная страница')).toBeInTheDocument();
        expect(screen.getByText('Профиль')).toBeInTheDocument();
        expect(screen.getByText('О проекте')).toBeInTheDocument();
    });

    test('Footer - показывает ссылки по умолчанию для неизвестного маршрута', () => {
        render(
            <MemoryRouter initialEntries={['/random-page']}>
                <Footer />
            </MemoryRouter>
        );

        expect(screen.getByText('Главная страница')).toBeInTheDocument();
        expect(screen.getByText('Генерация табулатуры')).toBeInTheDocument();
        expect(screen.getByText('О проекте')).toBeInTheDocument();
    });
});