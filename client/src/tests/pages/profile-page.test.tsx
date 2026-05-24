import { describe, expect, beforeEach, test, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';

import ProfilePage from '@/pages/profile-page/profile-page';

import { useAuthStore } from '@/store/authStore';

describe('ProfilePage', () => {
    beforeEach(() => {
        document.title = '';

        useAuthStore.setState({
            user: {
                id: 1,
                username: 'test',
                email: 't@example.com',
                createdAt: '2004-10-01'
            },
            logout: vi.fn()
        });
    });

    test('ProfilePage - рендерит информацию профиля и кнопки', () => {
        render(
            <MemoryRouter>
                <ProfilePage />
            </MemoryRouter>
        );

        expect(
            screen.getByRole('heading', {
                name: 'Профиль'
            })
        ).toBeInTheDocument();

        expect(
            screen.getByRole('heading', {
                level: 2,
                name: 'test'
            })
        ).toBeInTheDocument();

        expect(
            screen.getByText('t@example.com')
        ).toBeInTheDocument();

        expect(
            screen.getByRole('button', {
                name: /Выйти из профиля/i
            })
        ).toBeInTheDocument();

        expect(
            screen.getAllByRole('button', {
                name: /Удалить профиль/i
            })[0]
        ).toBeInTheDocument();

        expect(document.title).toBe('Профиль');
    });
});