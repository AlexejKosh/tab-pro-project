import { describe, expect, beforeEach, test } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import Header from '@/components/common/header/header';

import { useAuthStore } from '@/store/authStore';

describe('Header', () => {
    beforeEach(() => {
        useAuthStore.setState({
            isAuthenticated: false,
            user: null
        });
    });

    test('Header - отображает кнопку входа для гостя', () => {
        render(
            <MemoryRouter>
                <Header />
            </MemoryRouter>
        );
        expect(screen.getByText('Войти')).toBeInTheDocument();
    });

    test('Header - отображает имя пользователя для авторизованного', () => {
        useAuthStore.setState({
            isAuthenticated: true,
            user: {
                id: 1,
                username: 'Alex',
                email: 'alex@test.com',
                createdAt: '10-01-2004'
            }
        });
        render(
            <MemoryRouter>
                <Header />
            </MemoryRouter>
        );
        expect(screen.getByText('Alex')).toBeInTheDocument();
    });
});